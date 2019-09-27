package com.akka.wrapper.exception;

import java.util.HashMap;
import java.util.Map;

import javax.management.ServiceNotFoundException;
import javax.naming.ServiceUnavailableException;

import com.akka.wrapper.dto.ImportProcessStatus;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ExceptionToImportProcessStatusMapper {

    private static final Map<Class<?>, ImportProcessStatus> map = initializeMap();

    private static Map<Class<?>, ImportProcessStatus> initializeMap() {
        final Map<Class<?>, ImportProcessStatus> map = new HashMap<>();
        map.put(RestHandlerException.class, ImportProcessStatus.FAILURE_RECOVERABLE);
        map.put(JsonMappingException.class, ImportProcessStatus.FAILURE_NON_RECOVERABLE);
        map.put(InvalidSpecificationException.class, ImportProcessStatus.FAILURE_NON_RECOVERABLE);
        map.put(ServiceIOException.class, ImportProcessStatus.FAILURE_RECOVERABLE_WITH_DELAY);
        map.put(ServiceUnavailableException.class, ImportProcessStatus.FAILURE_RECOVERABLE_WITH_DELAY);
        map.put(ServiceNotFoundException.class, ImportProcessStatus.FAILURE_RECOVERABLE_WITH_DELAY);
        map.put(RecoverableProcessingException.class, ImportProcessStatus.FAILURE_RECOVERABLE_WITH_DELAY);
        map.put(CircuitBreakerOpenException.class, ImportProcessStatus.FAILED_DUE_TO_OPEN_CIRUICTBREAKER);
        map.put(ServiceException.class, ImportProcessStatus.FAILURE_NON_RECOVERABLE);
        return map;
    }

    public static ImportProcessStatus getMappedImportProcessStatus(final Class<?> exceptionClass) {
        return map.get(exceptionClass);
    }

}
