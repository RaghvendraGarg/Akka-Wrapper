package com.akka.wrapper.exception;

public class RecoverableProcessingException extends Exception {

    private static final long serialVersionUID = 3662551776124248907L;

    public RecoverableProcessingException() {
        super();
    }

    public RecoverableProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RecoverableProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RecoverableProcessingException(String message) {
        super(message);
    }

    public RecoverableProcessingException(Throwable cause) {
        super(cause);
    }

}
