package org.example.service;

import org.example.entity.Entry;
import org.example.exception.DbUnavaiableException;
import org.example.exception.InvalidKeyException;

import java.util.HashMap;

public class DbService  implements IDbservice{
    private final HashMap<Integer , Entry<Object>> data ;
    volatile private boolean state  ;

    final private int cleaupTime = 1000 ;


    public DbService() {

        data = new HashMap<>();
        state = true;
        backGroundTask();
    }

    synchronized private void cleanUp(){
        data.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void checkDbState() {
        if (!state) {
            throw new DbUnavaiableException("Unable to connect to Db");
        }
    }



    private  void backGroundTask(){

        System.out.println("Background clear started");

        Thread thread = new Thread(()->{

            while(true){

                try {
                    cleanUp() ;
                    Thread.sleep(cleaupTime);

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }) ;

        thread.setDaemon(true);
        thread.start();
    }





    @Override
    synchronized public void put(Integer key, Object data , long ttl) {

        checkDbState();

        Entry<Object> ob = new Entry<>(data , ttl)  ;
        this.data.put(key , ob) ;
    }

    @Override
    synchronized public  void put(Integer key, Object data) {

        checkDbState() ;
        Entry<Object> ob = new Entry<>(data)  ;
        this.data.put(key , ob) ;
    }


    // Lazy delete
    @Override
    synchronized public  Object get(Integer key) {
        checkDbState() ;
        if(data.containsKey(key)){
            if(data.get(key).isExpired()){
                data.remove(key)  ;
                throw new InvalidKeyException("Key not found or expired");
            }
           return data.get(key).data ;
        }
        else {
            throw new InvalidKeyException("Key not found");
        }
    }

    @Override
    synchronized public  void delete(Integer key) {
        checkDbState() ;
        if(data.containsKey(key)){
            data.remove(key) ;
        }
        else {
            throw new InvalidKeyException("Key not found");
        }
    }

    public void start(){
        this.state = true ;
    }

    public void stop(){
        this.state = false ;
    }

    public void showDB(){
        checkDbState();
        System.out.println(data);
    }


}
