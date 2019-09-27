package com.akka.wrapper.job.step;

import java.util.Optional;

import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.Job;
import com.akka.wrapper.job.JobExecutionContext;

/**
 * Created by gargr on 21/02/17.
 */
public class StepExecutionContext {

    private StepContext stepContext;

    private Job job;

    private MultiValueMap<String, PlatformMessage> messages;

    private boolean anyPreviousStepWasAccumulator;

    private JobExecutionContext jobExecutionContext;

    StepExecutionContext(StepContext stepContext, JobExecutionContext jobExecutionContext, PlatformMessage message) {
        this.stepContext = stepContext;
        this.job = jobExecutionContext.getJob();
        messages = new LinkedMultiValueMap<>();
        messages.add(message.getMessageId(), message);
        this.jobExecutionContext = jobExecutionContext;
    }

    StepExecutionContext(StepContext stepContext, JobExecutionContext jobExecutionContext, MultiValueMap<String, PlatformMessage> messages) {
        this.stepContext = stepContext;
        this.job = jobExecutionContext.getJob();
        this.messages = messages;
        this.jobExecutionContext = jobExecutionContext;
    }

    public StepContext getStepContext() {
        return stepContext;
    }

    public Job getJob() {
        return job;
    }

    public StepContext getNextStepContext() {
        Step nextStep = job.getNextStep(new Step(stepContext));
        return nextStep == null ? null : nextStep.getStepContext();
    }

    public MultiValueMap<String, PlatformMessage> getMessages() {
        return CollectionUtils.unmodifiableMultiValueMap(messages);
    }

    public PlatformMessage getMessage() {
        Optional<PlatformMessage> first = messages.entrySet().stream().
                map(v -> v.getValue()).
                flatMap(v -> v.stream()).
                map(v -> v).findFirst();
        return first.orElse(null);
    }

    public boolean isAnyPreviousStepWasAccumulator() {
        return anyPreviousStepWasAccumulator;
    }

    void setAnyPreviousStepWasAccumulator(boolean anyPreviousStepWasAccumulator) {
        this.anyPreviousStepWasAccumulator = anyPreviousStepWasAccumulator;
    }

    public JobExecutionContext getJobExecutionContext() {
        return jobExecutionContext;
    }
}
