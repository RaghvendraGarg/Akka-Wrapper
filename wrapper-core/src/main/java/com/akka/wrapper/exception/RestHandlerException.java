package com.akka.wrapper.exception;

public class RestHandlerException extends Exception {

    private static final long serialVersionUID = -522857569051121999L;

    public RestHandlerException(String message) {
        super(message);
    }

    public RestHandlerException(Throwable cause) {
        super(cause);
    }

    public RestHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
