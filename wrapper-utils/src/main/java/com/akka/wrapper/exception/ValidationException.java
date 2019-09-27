package com.akka.wrapper.exception;

public class ValidationException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the ValidationException object.
     *
     * @param message
     *            the exception detail message
     */
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Exception exception) {
        super(message, exception);
    }
}
