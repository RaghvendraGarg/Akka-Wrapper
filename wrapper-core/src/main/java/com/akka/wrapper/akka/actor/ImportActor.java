package com.akka.wrapper.akka.actor;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;

import com.akka.wrapper.dto.ImportProcessStatus;
import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.exception.ServiceClassNotPresentException;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.listener.AfterJobExecutionListener;
import com.akka.wrapper.job.listener.AfterJobExecutionListenerContext;
import com.akka.wrapper.job.step.ForkedStep;
import com.akka.wrapper.job.step.StepContext;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;
import com.akka.wrapper.service.Service;
import com.akka.wrapper.status.ImportProcessStatusFetcher;

import akka.actor.AbstractActor;
import akka.actor.Props;

/**
 * Created by gargr on 10/02/17.
 */
public final class ImportActor extends AbstractActor {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Service tService;

    private ImportActorHelper importActorHelper;

    /**
     * Name of the step for which this actor is used, Each actor should have a unique step,
     * this will be used for logging and monitoring purpose.
     */
    private String processingStepName;

    private ImportActor(Service tService, String processingStepName, ImportActorHelper importActorHelper) {
        this.tService = tService;
        this.processingStepName = processingStepName;
        this.importActorHelper = importActorHelper;
    }

    /**
     * Create Props for an actor of this type.
     *
     * @param tService Is the class which performs the all the business operations Should always be SINGLETON
     * @return a Props for creating this actor, which can then be further configured
     * (e.g. calling `.withDispatcher()` on it)
     */
    protected static Props props(final Service tService, final String processingStepName, final ImportActorHelper importActorHelper) {
        return Props.create(ImportActor.class, () -> new ImportActor(tService, processingStepName, importActorHelper));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StepExecutionContext.class, context -> {
                    StopWatch stopWatch = new StopWatch();
                    Job job = context.getJob();
                    AfterJobExecutionListener afterJobExecutionListener = job.getAfterJobExecutionListener();
                    final Object t = getMessage(context);
                    try {
                        stopWatch.start();
                        act(t);
                        initiateNextStep(context);
                        stopWatch.stop();
                        if (logger.isDebugEnabled()) {
                            logger.debug("time taken : {} for messageId : {}, and for step {}", stopWatch.getTotalTimeMillis(), afterJobExecutionListener.getMessageIds(t), processingStepName);
                        }
                    } catch (final Throwable e) {
                        logger.error("exception occurred while processing message {}  at step {} {}", afterJobExecutionListener.getMessageIds(t), processingStepName, e.getMessage(), e);
                        AfterJobExecutionListenerContext j = new AfterJobExecutionListenerContext(ImportProcessStatusFetcher.getImportProcessStatus(e.getClass()), processingStepName, "failed", t, getTotalTimeMillis(context));
                        afterJobExecutionListener.onFailure(j);
                    } finally {
                        if (stopWatch.isRunning()) {
                            stopWatch.stop();
                        }
                    }
                })
                .build();
    }


    private Object getMessage(StepExecutionContext context) {
        if (context.isAnyPreviousStepWasAccumulator()) {
            return context.getMessages();
        }
        return context.getMessage();
    }

    private void act(Object t) throws Exception {
        if (tService == null) {
            throw new ServiceClassNotPresentException("Service class cannot be null");
        }
        tService.apply(t);
    }

    private void initiateNextStep(StepExecutionContext context) {
        StepContext currentStepContext = context.getStepContext();
        StepContext nextStepContext = context.getNextStepContext();
        if (currentStepContext.getTerminatingCondition() != null && currentStepContext.getTerminatingCondition().test(getMessage(context))) {
            finishJob(context);
            return;
        }
        List<ForkedStep> jobs = currentStepContext.getJobs();
        if (!CollectionUtils.isEmpty(jobs)) {
            PlatformMessage message = (PlatformMessage) getMessage(context);
            Optional<ForkedStep> first = jobs.stream().filter(j -> j.getPredicate().test(getMessage(context))).findFirst();
            if (first.isPresent()) {
                ForkedStep forkedStep = first.get();
                Job job = forkedStep.getJob();
                importActorHelper.getJobInitiatorActorRef().tell(importActorHelper.getJobExecutionContextFactory().create(job.getName(), message, context.getJobExecutionContext().getStopWatch()), self());
                return;
            }
        }

        callNextActor(nextStepContext, context);
    }

    private void callNextActor(StepContext nextStep, StepExecutionContext context) {
        if (nextStep != null && nextStep.getActorRef() != null) {
            StepContext currentStepContext = context.getStepContext();
            StepExecutionContext stepExecutionContext;
            if (currentStepContext.isPreviousStepWasBunchingStep()) {
                stepExecutionContext = StepExecutionContextFactory.create(nextStep, context.getJobExecutionContext(), (MultiValueMap<String, PlatformMessage>) getMessage(context));
            } else {
                stepExecutionContext = StepExecutionContextFactory.create(nextStep, context.getJobExecutionContext(), (PlatformMessage) getMessage(context));
            }
            nextStep.getActorRef().tell(stepExecutionContext, self());
        } else {
            finishJob(context);
        }
    }

    private void finishJob(StepExecutionContext context) {
        long totalTimeMillis = getTotalTimeMillis(context);
        AfterJobExecutionListenerContext j = new AfterJobExecutionListenerContext(ImportProcessStatus.SUCCESS, processingStepName, "Success", getMessage(context), totalTimeMillis);
        context.getJob().getAfterJobExecutionListener().onSuccess(j);
    }

    private long getTotalTimeMillis(StepExecutionContext context) {
        StopWatch stopWatch = context.getJobExecutionContext().getStopWatch();
        if (stopWatch.isRunning()) {
            stopWatch.stop();
        }
        return stopWatch.getTotalTimeMillis();
    }

}
