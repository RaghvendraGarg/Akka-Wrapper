package com.akka.wrapper.exception;

/**
 * Created by gargr on 10/02/17.
 */
public class ServiceClassNotPresentException extends Exception {

    public ServiceClassNotPresentException() {
        super();
    }

    public ServiceClassNotPresentException(String message) {
        super(message);
    }

    public ServiceClassNotPresentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceClassNotPresentException(Throwable cause) {
        super(cause);
    }

    protected ServiceClassNotPresentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
