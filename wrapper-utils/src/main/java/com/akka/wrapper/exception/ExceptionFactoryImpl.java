package com.akka.wrapper.exception;


import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Factory class to create {@link ApplicationException} object and to log the Exception cause.
 */
@Service("importConfigExceptionFactory")
public class ExceptionFactoryImpl implements ExceptionFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExceptionFactoryImpl.class);

    @Override
    public RuntimeException createRuntimeExceptionStampedWithUniqueID(String message, Throwable originalException) {
        String message2print = prepareMessagePrefix(message) + "UniqueId: " + createUniqueID(originalException);
        return new RuntimeException(message2print, originalException);
    }

    @Override
    public ApplicationException createApplicationException(Exception e) {
        ApplicationException applicationException = buildApplicationException(e);
        ErrorMessage errorMessage = applicationException.getErrorObject();
        logException(errorMessage, e);
        return applicationException;
    }

    private void logException(ErrorMessage errorMessage, Exception e) {
        ExceptionResponseStatus exceptionResponseStatus = ExceptionResponseStatus.parse(e.getClass());
        String message = errorMessage.getUUID() + " " + e.getMessage();
        if (exceptionResponseStatus != null) {
            getLogger().info(message, e);
        } else {
            getLogger().error(message, e);
        }
    }

    private boolean cannotProcessThrowable(Throwable ex) {
        return ex == null || StringUtils.isEmpty(ex.getMessage());
    }

    private long createUniqueID(Throwable ex) {
        return cannotProcessThrowable(ex) ? -1 : ex.getMessage().hashCode();
    }

    private String prepareMessagePrefix(String message) {
        return StringUtils.isEmpty(message) ? "" : message + ", ";
    }

    private ApplicationException buildApplicationException(Exception e) {
        String customMessage = ExceptionResponseStatus.getMappedMessage(e);
        return new ApplicationException(customMessage, e);
    }

    @Override
    public String getExceptionCause(final Exception ex) {
        if (null == ex) {
            return null;
        }
        if (ex.getCause() == null) {
            return ex.toString();
        }
        String exceptionMessage = ex.getCause().toString();
        exceptionMessage = exceptionMessage.replaceAll("\\n", "");
        return exceptionMessage;
    }

    @Override
    public String getExceptionMessage(final Exception ex) {
        if (null == ex) {
            return null;
        }
        if (ex.getMessage() == null) {
            return ex.toString();
        }
        String exceptionMessage = ex.getMessage().toString();
        exceptionMessage = exceptionMessage.replaceAll("\\n", "");
        return exceptionMessage;
    }

    Logger getLogger() {
        return LOGGER;
    }
}
