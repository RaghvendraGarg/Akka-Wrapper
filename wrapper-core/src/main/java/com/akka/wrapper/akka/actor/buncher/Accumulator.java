package com.akka.wrapper.akka.actor.buncher;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.dto.PlatformMessage;
import com.akka.wrapper.job.step.StepContext;
import com.akka.wrapper.job.step.StepExecutionContext;
import com.akka.wrapper.job.step.StepExecutionContextFactory;

import akka.actor.AbstractActor;
import akka.actor.Cancellable;
import akka.actor.Props;
import scala.concurrent.duration.Duration;

/**
 * This is generic implementation of Router Accumulator.
 * This provides functionality of bunching objects based on time or no. of objects collected,
 * Also Objects can be collected based on the key
 */
public class Accumulator extends AbstractActor {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static PlatformMessage FLUSH_MESSAGE = new PlatformMessage() {
    };

    private MultiValueMap<String, PlatformMessage> messages;

    private Cancellable tick = null;

    private int flushSize;

    private long flushInterval;

    private Function<PlatformMessage, String> getKey;

    private StepExecutionContext current;

    private Accumulator(int flushSize, long flushInterval, Function getKey) {
        this.flushSize = flushSize;
        this.flushInterval = flushInterval;
        this.getKey = getKey;
        messages = new LinkedMultiValueMap<>();
    }

    public static Props props(final int flushSize, final long flushInterval, final Function getKey) {
        return Props.create(Accumulator.class, () -> new Accumulator(flushSize, flushInterval, getKey));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals(FLUSH_MESSAGE, message -> {
            if (current != null) {
                flushQueue();
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Flushed messages ticked by time for messageId's {}", getMessageIdsForBunchedRequest());
            }
        }).match(StepExecutionContext.class, stepExecutionContext -> {
            try {
                if (current == null) {
                    setStepExecutionContext(stepExecutionContext);
                }
                if (messages.size() == 0) {
                    resetTick();
                }
                PlatformMessage importMessage = stepExecutionContext.getMessage();
                if (!pushMessageIntoQueue(importMessage)) {
                    flushQueue();
                    getSelf().tell(stepExecutionContext, getSelf());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Flushed messages ticked by number of vehicles for messageId's {}", getMessageIdsForBunchedRequest());
                    }
                }
            } catch (final Throwable e) {
                logger.error("exception occurred while Bunching messages {} {}", getMessageIdsForBunchedRequest(), e.getMessage(), e);
            }
        }).build();
    }

    private boolean pushMessageIntoQueue(PlatformMessage message) {
        if (size() < flushSize) {
            messages.add(getKey.apply(message), message);
            return true;
        }
        return false;
    }

    private void flushQueue() {
        if (messages != null && !messages.isEmpty()) {
            StepContext nextStepContext = current.getNextStepContext();
            StepExecutionContext stepExecutionContext = StepExecutionContextFactory.create(nextStepContext, current.getJobExecutionContext(), messages);
            nextStepContext.getActorRef().tell(stepExecutionContext, getSelf());
            messages = new LinkedMultiValueMap<>();
        }
    }

    private int size() {
        return messages.entrySet().stream().mapToInt(k -> messages.get(k.getKey()).size()).sum();
    }

    private String getMessageIdsForBunchedRequest() {
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

    private void resetTick() {
        if (tick != null && !tick.isCancelled()) {
            tick.cancel();
        }
        tick = getContext().system().scheduler().scheduleOnce(Duration.create(flushInterval, TimeUnit.MILLISECONDS), getSelf(), FLUSH_MESSAGE, getContext().dispatcher(), null);
    }

    private void setStepExecutionContext(StepExecutionContext message) {
        current = StepExecutionContextFactory.create(message.getStepContext(), message.getJobExecutionContext(), FLUSH_MESSAGE);
    }

}
