package com.akka.wrapper.job.listener;

import com.akka.wrapper.dto.ImportProcessStatus;

/**
 * Created by gargr on 03/03/17.
 */
public class AfterJobExecutionListenerContext<T> {

    private ImportProcessStatus status;

    private String processingStep;

    private String message;

    private T object;

    private long timeTaken;

    public AfterJobExecutionListenerContext(ImportProcessStatus status, String processingStep, String message, T object, long timeTaken) {
        this.status = status;
        this.processingStep = processingStep;
        this.message = message;
        this.object = object;
        this.timeTaken = timeTaken;
    }

    public ImportProcessStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getObject() {
        return object;
    }

    public String getProcessingStep() {
        return processingStep;
    }

    public long getTimeTaken() {
        return timeTaken;
    }
}
