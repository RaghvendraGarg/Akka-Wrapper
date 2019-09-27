package com.akka.wrapper.akka.actor.buncher;

import java.util.function.Function;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.SmallestMailboxPool;

/**
 * Created by gargr on 22/02/17.
 */
@Component
public class AccumulatorFactory {

    private static final Logger logger = LoggerFactory.getLogger(AccumulatorFactory.class);

    @Resource
    private ActorSystem actorSystem;

    @Resource(name = "splittingActorRef")
    private ActorRef splittingActorRef;

    /**
     * Creates an ActorRef bean from {@link Accumulator} and with the following parameters. Accumulator will accumulate
     * noOfMessages and publish to next Actor if the the noOfMessages has reached or flushInterval has passed.
     * @param buncherName
     * @param noOfMessages
     * @param flushInterval
     * @param fetchKey Accumulator stores data in a Map, Function to fetch Key is required
     * @param noOfInstances no of instances for Accumulator
     * @return ActorRef
     */
    public ActorRef createAccumulator(String buncherName, int noOfMessages, long flushInterval, Function fetchKey, int noOfInstances) {
        logger.debug("creating Accumulator bean for actorName {}, noOfMessages {}, flushInterval {}, processingStep {}", buncherName, noOfMessages, flushInterval, buncherName);
        ActorRef actorRef = actorSystem.actorOf(Accumulator.props(noOfMessages, flushInterval, fetchKey).withRouter(new SmallestMailboxPool(noOfInstances)), buncherName);
        return actorRef;
    }

    public ActorRef getSplittingActorRef() {
        return splittingActorRef;
    }

}
