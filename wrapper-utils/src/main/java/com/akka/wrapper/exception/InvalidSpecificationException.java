package com.akka.wrapper.exception;

public class InvalidSpecificationException extends RuntimeException {


    private static final long serialVersionUID = -3872618108634774647L;

    public InvalidSpecificationException() {
        super();
    }

    public InvalidSpecificationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidSpecificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSpecificationException(String message) {
        super(message);
    }

    public InvalidSpecificationException(Throwable cause) {
        super(cause);
    }

}
