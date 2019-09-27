package com.akka.wrapper.exception;

public class ServiceIOException extends Exception {

    private static final long serialVersionUID = -6687583476165193309L;

    public ServiceIOException() {
        super();
    }

    public ServiceIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ServiceIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceIOException(String message) {
        super(message);
    }

    public ServiceIOException(Throwable cause) {
        super(cause);
    }

}
