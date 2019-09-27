package com.akka.wrapper.job;

import akka.actor.ActorRef;
import com.akka.wrapper.dto.PlatformMessage;
import javax.annotation.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by gargr on 11/04/17.
 */
@Component
public class JobInitiatorImpl implements JobInitiator {

    @Resource
    private JobExecutionContextFactory jobExecutionContextFactory;

    @Resource(name = "jobInitiatorActorRef")
    private ActorRef jobInitiatorRef;

    @Override
    public void initiate(String jobName, PlatformMessage message) {
        validate(jobName, message);
        JobExecutionContext jobExecutionContext = jobExecutionContextFactory.create(jobName, message);
        jobInitiatorRef.tell(jobExecutionContext, null);
    }

    private void validate(String jobName, PlatformMessage message) {
        if(StringUtils.isBlank(jobName) || message == null){
            throw new IllegalArgumentException("jobName or message cannot be null or empty");
        }
    }
}
