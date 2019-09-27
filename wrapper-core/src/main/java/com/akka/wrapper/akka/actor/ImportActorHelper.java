package com.akka.wrapper.akka.actor;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.akka.wrapper.job.JobExecutionContextFactory;

import akka.actor.ActorRef;

/**
 * Created by gargr on 02/03/17.
 */
@Component
public class ImportActorHelper {

    @Resource
    private ActorRef jobInitiatorActorRef;

    @Resource
    private JobExecutionContextFactory jobExecutionContextFactory;

    ActorRef getJobInitiatorActorRef() {
        return jobInitiatorActorRef;
    }

    JobExecutionContextFactory getJobExecutionContextFactory() {
        return jobExecutionContextFactory;
    }
}
