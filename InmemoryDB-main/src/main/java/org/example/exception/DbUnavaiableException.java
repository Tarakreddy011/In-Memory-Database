package org.example.exception;

public class DbUnavaiableException extends RuntimeException {

    public DbUnavaiableException(String message) {
        super(message);
    }

}
