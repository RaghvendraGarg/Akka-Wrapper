package com.akka.wrapper.job.step;

import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.JobExecutionContext;
import org.springframework.util.MultiValueMap;

/**
 * Created by gargr on 27/02/17.
 */
public class StepExecutionContextFactory {

    public static StepExecutionContext create(StepContext context, JobExecutionContext jobExecutionContext, MultiValueMap<String, PlatformMessage> messages){
        StepExecutionContext executionContext1 = new StepExecutionContext(context, jobExecutionContext, messages);
        executionContext1.setAnyPreviousStepWasAccumulator(true);
        return executionContext1;
    }

    public static StepExecutionContext create(StepContext context, JobExecutionContext jobExecutionContext, PlatformMessage message){
        StepExecutionContext executionContext1 = new StepExecutionContext(context, jobExecutionContext, message);
        executionContext1.setAnyPreviousStepWasAccumulator(false);
        return executionContext1;
    }

}
