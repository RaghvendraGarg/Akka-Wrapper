package com.akka.wrapper.akka.actor;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StopWatch;

import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobExecutionContext;
import com.akka.wrapper.job.step.Step;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;

import akka.actor.AbstractActor;

/**
 * Created by gargr on 16/02/17.
 */
@Named("jobInitiatorActor")
@Scope("prototype")
public class JobInitiatorActor extends AbstractActor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(JobExecutionContext.class, context -> {
            Job job = context.getJob();
            Step firstStep = job.getFirstStep();
            StepExecutionContext firstStepContext = StepExecutionContextFactory.create(firstStep.getStepContext(), context, context.getMessage());
            startStopWatch(context);
            firstStep.getStepContext().getActorRef().tell(firstStepContext, getSelf());
        }).build();
    }

    private void startStopWatch(JobExecutionContext context) {
        StopWatch stopWatch = context.getStopWatch();
        if (!stopWatch.isRunning()) {
            stopWatch.start();
        }
    }

}
