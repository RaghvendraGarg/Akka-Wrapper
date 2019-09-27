package com.akka.wrapper.exception;



/**
 * Custom Exception for exceptions thrown by Vehicle CRUD REST API
 * 
 * @author changedm
 * 
 */
public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorMessage errorObject;

    /**
     * Constructor for the BundlingException object.
     */
    public ApplicationException() {
        super();
        errorObject = createErrorObject(getMessage(), this.getClass());
    }

    /**
     * Constructor for the DecoratedVehicleSearchException object.
     * 
     * @param message
     *            the exception detail message
     */
    public ApplicationException(String message) {
        super(message);
        errorObject = createErrorObject(message, this.getClass());
    }

    /**
     * Constructs a new ApplicationException with the specified
     * detail message and cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this
     * runtime exception's detail message.
     * 
     * @param message
     *            the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
     *            value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        errorObject = createErrorObject(message, cause.getClass());
    }

    /**
     * Constructs a new ApplicationException with the specified cause
     * and a detail message of <tt>(cause==null ? null : cause.toString())</tt> (which typically contains the class and
     * detail message of <tt>cause</tt> ). This constructor is useful for runtime exceptions that are little more
     * than wrappers for other throwables.
     * 
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
     *            value is
     *            permitted, and indicates that the cause is nonexistent or
     *            unknown.)
     */
    public ApplicationException(Throwable cause) {
        super(cause);
        errorObject = createErrorObject(cause.getMessage(), cause.getClass());
    }

    private ErrorMessage createErrorObject(String message, Class<?> exceptionClass) {
        return ErrorMessageFactory.create(message, exceptionClass, hashCode());
    }

    public ErrorMessage getErrorObject() {
        return errorObject;
    }

}
