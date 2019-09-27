package com.akka.wrapper.job;

import org.springframework.util.StopWatch;

import com.akka.wrapper.dto.PlatformMessage;

/**
 * Created by gargr on 20/02/17.
 */
public class JobExecutionContext {

    private Job job;

    PlatformMessage message;

    private StopWatch stopWatch;

    JobExecutionContext(Job job, PlatformMessage message, StopWatch stopWatch) {
        this.job = job;
        this.message = message;
        this.stopWatch = stopWatch;
    }

    public Job getJob() {
        return job;
    }

    public PlatformMessage getMessage() {
        return message;
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

}
