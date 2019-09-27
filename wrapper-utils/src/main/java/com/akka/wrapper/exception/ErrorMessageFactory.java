package com.akka.wrapper.exception;

public class ErrorMessageFactory {

    private static final String CONCAT = "_";

    private static int getResponseCode(Class<?> exceptionClass) {
        return ExceptionResponseStatus.getMappedStatusCode(exceptionClass).value();
    }

    /**
     * create ErrorMessage object using given message, exception and seed to generate uuid
     * 
     * @param message
     * @param exceptionClass
     * @return ErrorMessage
     */
    public static ErrorMessage create(String message, Class<?> exceptionClass, int seed) {
        ErrorMessage errorMessage = new ErrorMessage();
        String uuid  = String.valueOf(String.valueOf(seed)) + CONCAT + String.valueOf(System.currentTimeMillis());
        errorMessage.setMessage(message);
        errorMessage.setUUID(uuid);
        errorMessage.setHTTPCode(getResponseCode(exceptionClass));
        return errorMessage;
    }
}
