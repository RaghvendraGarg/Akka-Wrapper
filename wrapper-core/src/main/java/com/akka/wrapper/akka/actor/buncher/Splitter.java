package com.akka.wrapper.akka.actor.buncher;

/**
 * Created by gargr on 22/02/17.
 */

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.step.StepContext;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;

import akka.actor.AbstractActor;

/**
 * Actor that takes the bunched request that flowed out of {@link Accumulator} and then
 * publish individual messages to next actor in chain.
 */
@Named("splittingActor")
@Scope("prototype")
public class Splitter extends AbstractActor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(StepExecutionContext.class, stepExecutionContext -> {
            String messageIds = null;
            try {
                StepContext nextStepContext = stepExecutionContext.getNextStepContext();
                if (stepExecutionContext.isAnyPreviousStepWasAccumulator()) {
                    MultiValueMap<String, PlatformMessage> messages = stepExecutionContext.getMessages();
                    if (!CollectionUtils.isEmpty(messages)) {
                        messageIds = getMessageIdsForBunchedRequest(messages);
                        messages.forEach((k, v) -> {
                            v.forEach(i -> {
                                nextStepContext.getActorRef().tell(StepExecutionContextFactory.create(nextStepContext, stepExecutionContext.getJobExecutionContext(), i), getSelf());
                            });
                        });
                    }
                } else {
                    messageIds = stepExecutionContext.getMessage().getMessageId();
                    nextStepContext.getActorRef().tell(StepExecutionContextFactory.create(nextStepContext, stepExecutionContext.getJobExecutionContext(), stepExecutionContext.getMessage()), getSelf());
                }
            } catch (Throwable t) {
                logger.error("exception occurred while Splitting messages {} {}", messageIds, t.getMessage(), t);
            }
        }).build();
    }

    private String getMessageIdsForBunchedRequest(MultiValueMap<String, PlatformMessage> messages) {
        List<String> messageIds = messages.entrySet().stream().
                map(v -> v.getValue()).
                flatMap(v -> v.stream()).
                map(v -> v.getMessageId()).
                collect(Collectors.toList());
        if (messageIds != null && !messageIds.isEmpty()) {
            return StringUtils.join(messageIds, ",");
        }
        return null;
    }

}
