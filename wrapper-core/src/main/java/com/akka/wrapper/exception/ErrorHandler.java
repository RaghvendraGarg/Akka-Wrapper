package com.akka.wrapper.exception;

/**
 * Created by gargr on 3/1/17.
 */
public interface ErrorHandler {

    public void handleError(Throwable ex, Object message);
}
