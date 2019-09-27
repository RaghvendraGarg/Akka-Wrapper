package com.akka.wrapper.exception;

/**
 * Factory class to create {@link ApplicationException}
 */
public interface ExceptionFactory {

    /**
     * Creates a new runtime exception stamped with a unique identifier. The unique id is created based on the original
     * exception's message's hashcode.
     * 
     * @param message user supplied message... null or empty messages will be ignored.
     * @param originalException the throwable that you want stamped with unique id
     * @return exception containing the unique id.
     */
    RuntimeException createRuntimeExceptionStampedWithUniqueID(String message, Throwable originalException);

    /**
     * Creates ApplicationException
     * 
     * @param e
     * @return
     */
    ApplicationException createApplicationException(Exception e);

    /**
     * Returns the exception cause in case of database update operation fails
     * 
     * @param e
     * @return exceptionCause
     */
    String getExceptionCause(Exception e);

    /**
     * Returns the exception message in case of database update operation fails
     * 
     * @param e
     * @return exception message
     */
    String getExceptionMessage(Exception e);
}
