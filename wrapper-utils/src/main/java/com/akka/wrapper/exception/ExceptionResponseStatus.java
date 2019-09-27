package com.akka.wrapper.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * Enum to hold HttpStatus to Exception class Mappings and also provides utility methods to get the mapped status and
 * exception class.
 */
public enum ExceptionResponseStatus {

    INVALID_SPECIFICATION (HttpStatus.BAD_REQUEST, InvalidSpecificationException.class),
    INVALID_INPUT (HttpStatus.BAD_REQUEST, ValidationException.class),
    ILLEGAL_ARGUMENT (HttpStatus.INTERNAL_SERVER_ERROR, IllegalArgumentException.class),
    INVALID_NUMBER (HttpStatus.UNPROCESSABLE_ENTITY, NumberFormatException.class);

    private HttpStatus statusCode;

    private Class<?> exceptionClass;

    private static final String INTERNAL_SERVER_ERROR = "Internal server error";

    private static final Map<Class<?>, ExceptionResponseStatus> ENUM_MAPPING = new HashMap<>(values().length);

    static {
        for (ExceptionResponseStatus exceptionResponseStatus : values()) {
            ENUM_MAPPING.put(exceptionResponseStatus.exceptionClass, exceptionResponseStatus);
        }
    }

    private ExceptionResponseStatus(HttpStatus statusCode, Class<?> exceptionClass) {
        this.statusCode = statusCode;
        this.exceptionClass = exceptionClass;
    }

    public static HttpStatus getMappedStatusCode(Class<?> key) {
        ExceptionResponseStatus exceptionResponseStatus = ENUM_MAPPING.get(key);
        if (null != exceptionResponseStatus) {
            return exceptionResponseStatus.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public static String getMappedMessage(Exception exception) {
        if (null == exception) {
            throw new IllegalStateException("Exception cannot be null");
        }
        ExceptionResponseStatus exceptionResponseStatus = ENUM_MAPPING.get(exception.getClass());
        if (null != exceptionResponseStatus) {
            return exception.getMessage();
        }
        return INTERNAL_SERVER_ERROR;
    }

    public static HttpStatus getMappedMessageCode(Exception exception) {
        if (null == exception) {
            throw new IllegalStateException("Exception cannot be null");
        }
        ExceptionResponseStatus exceptionResponseStatus = ENUM_MAPPING.get(exception.getClass());
        if (null != exceptionResponseStatus) {
            return exceptionResponseStatus.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public static ExceptionResponseStatus parse(Class<?> key) {
        return ENUM_MAPPING.get(key);
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public Class<?> getExceptionClass() {
        return exceptionClass;
    }

}
