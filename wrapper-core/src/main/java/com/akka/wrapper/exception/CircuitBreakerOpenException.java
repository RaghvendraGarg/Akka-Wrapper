package com.akka.wrapper.exception;

public class CircuitBreakerOpenException extends Exception {

    private static final long serialVersionUID = -730317229633512165L;

    public CircuitBreakerOpenException() {
    }

    public CircuitBreakerOpenException(String message) {
        super(message);
    }

    public CircuitBreakerOpenException(Throwable cause) {
        super(cause);
    }

    public CircuitBreakerOpenException(String message, Throwable cause) {
        super(message, cause);
    }

    public CircuitBreakerOpenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
