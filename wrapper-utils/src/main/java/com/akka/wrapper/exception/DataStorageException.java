package com.akka.wrapper.exception;

/**
 * Created by kondetib on 10/9/15.
 */
public class DataStorageException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>Exception</code> instance.
     *
     * @param message
     *            the message to display
     * @param Exception
     *            the chained exception
     */
    public DataStorageException(String message, Throwable ex) {
        super(message, ex);
    }

}