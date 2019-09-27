package com.akka.wrapper.exception;

/**
 * Created by gargr on 01/03/17.
 */
public class InvalidStepException extends Exception {

    public InvalidStepException() {
        super();
    }

    public InvalidStepException(String message) {
        super(message);
    }

    public InvalidStepException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidStepException(Throwable cause) {
        super(cause);
    }

    protected InvalidStepException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
