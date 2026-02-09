package org.example.entity;

public class Entry<T>{
    public T data ;
    public long expiryTime ;


    @Override
    public String toString() {
        return data + "(" + expiryTime + ")";
    }

    public Entry(T data, long ttl) {
        this.data = data ;
        this.expiryTime = System.currentTimeMillis() +  ttl;
    }

    public Entry(T data) {
        this.data = data ;
        this.expiryTime =  -1 ;
    }

    public boolean isExpired(){
        if (expiryTime == -1){
            return false ;
        }
        return System.currentTimeMillis() > expiryTime ;
    }
}
