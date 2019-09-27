package com.akka.wrapper.exception;

/**
 * Created by thakurj on 3/1/17.
 */
public class DefaultErrorHandler implements ErrorHandler{

    @Override
    public void handleError(Throwable ex,Object message) {
        return;
    }
}
