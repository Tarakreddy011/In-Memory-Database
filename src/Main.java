import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {

        InMemoryDB<String> db = new InMemoryDB<>();

        ExecutorService pool = Executors.newFixedThreadPool(4);

        String[] commands = {
                "PUT 1 hello",
                "PUT 2 world 3000",
                "GET 1",
                "PUT 1 overwrite",
                "GET 2",
                "DELETE 1",
                "PUT 3 java",
                "GET 3",
                "STOP",
                "PUT 5 fail",
                "START",
                "PUT 5 works",
                "GET 5"
        };

        for(String cmd : commands)
            pool.submit(new CommandExecutor(db, cmd));

        pool.shutdown();
    }
}

class InMemoryDB<T> {

    private ConcurrentHashMap<Integer, Entry<T>> map = new ConcurrentHashMap<>();
    private volatile boolean running = true;

    public InMemoryDB() {
        startCleaner();
    }

    public void put(Integer key, T value, Long ttl) {
        checkRunning();
        map.put(key, new Entry<>(value, ttl == null ? -1 : ttl));
    }

    public T get(Integer key) {
        Entry<T> e = map.get(key);

        if(e == null)
            return null;

        if(e.isExpired()) {
            map.remove(key);
            return null;
        }
        return e.value;
    }

    public void delete(Integer key) {
        checkRunning();
        if(map.remove(key) == null)
            throw new KeyNotFoundException();
    }

    public void stop() { running = false; }
    public void start() { running = true; }

    private void checkRunning() {
        if(!running)
            throw new DatabaseStoppedException();
    }


    private void startCleaner() {

        Thread cleaner = new Thread(() -> {
            while(true) {
                try {
                    for(Integer k : map.keySet()) {
                        Entry<T> e = map.get(k);
                        if(e != null && e.isExpired())
                            map.remove(k);
                    }
                    Thread.sleep(1000);
                } catch(Exception ignored){}
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }
}


class Entry<T> {

    T value;
    long expiryTime;

    public Entry(T value, long ttlMillis) {
        this.value = value;
        this.expiryTime = ttlMillis > 0
                ? System.currentTimeMillis() + ttlMillis
                : -1;
    }

    boolean isExpired() {
        return expiryTime != -1 &&
                System.currentTimeMillis() > expiryTime;
    }
}


class CommandExecutor implements Runnable {

    private InMemoryDB<String> db;
    private String command;

    public CommandExecutor(InMemoryDB<String> db, String command) {
        this.db = db;
        this.command = command;
    }

    public void run() {
        try {

            Command c = CommandParser.parse(command);

            switch(c.type) {

                case PUT:
                    db.put(c.key, c.rawValue, c.ttl);
                    break;

                case GET:
                    System.out.println(Thread.currentThread().getName()
                            +" -> "+db.get(c.key));
                    break;

                case DELETE:
                    db.delete(c.key);
                    break;

                case STOP:
                    db.stop();
                    break;

                case START:
                    db.start();
                    break;

                case EXIT:
                    System.exit(0);
            }

        } catch(Exception e) {
            System.out.println(Thread.currentThread().getName()
                    +" ERROR: "+e.getMessage());
        }
    }
}


class CommandParser {

    static Command parse(String input)
            throws InvalidCommandException, InvalidTTLException {

        String[] t = input.trim().split("\\s+");

        try {
            CommandType type = CommandType.valueOf(t[0].toUpperCase());

            switch(type) {

                case PUT:
                    Integer key = Integer.parseInt(t[1]);
                    String val = t[2];

                    if(t.length == 4) {
                        long ttl = Long.parseLong(t[3]);
                        if(ttl <= 0)
                            throw new InvalidTTLException("TTL must be > 0");
                        return new Command(type,key,val,ttl);
                    }
                    return new Command(type,key,val,null);

                case GET:
                case DELETE:
                    return new Command(type,
                            Integer.parseInt(t[1]),
                            null,null);

                default:
                    return new Command(type,null,null,null);
            }

        } catch(NumberFormatException e){
            throw new InvalidCommandException("Key must be integer");

        } catch(ArrayIndexOutOfBoundsException e){
            throw new InvalidCommandException("Incomplete command");

        } catch(IllegalArgumentException e){
            throw new InvalidCommandException("Unknown command");
        }
    }
}


class Command {
    CommandType type;
    Integer key;
    String rawValue;
    Long ttl;

    Command(CommandType t, Integer k, String v, Long ttl){
        this.type=t;
        this.key=k;
        this.rawValue=v;
        this.ttl=ttl;
    }
}

enum CommandType {
    PUT, GET, DELETE, START, STOP, EXIT
}

class InvalidCommandException extends Exception {
    public InvalidCommandException(String m){ super(m); }
}

class InvalidTTLException extends Exception {
    public InvalidTTLException(String m){ super(m); }
}

class DatabaseStoppedException extends RuntimeException {
    public DatabaseStoppedException(){
        super("Database is STOPPED");
    }
}

class KeyNotFoundException extends RuntimeException {
    public KeyNotFoundException(){
        super("Key not found");
    }
}
