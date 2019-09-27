package com.akka.wrapper.akka.actor;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.akka.wrapper.service.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import akka.routing.SmallestMailboxPool;

/**
 * Created by gargr on 10/02/17.
 */
@Component
public class ActorFactoryImpl implements ActorFactory {

    private static final Logger logger = LoggerFactory.getLogger(ActorFactoryImpl.class);

    @Resource
    private ActorSystem actorSystem;

    @Resource
    private ImportActorHelper importActorHelper;

    @Override
    public ActorRef create(String actorName, Service service, String processingStep, int noOfInstances) {
        logger.debug("creating actorRef bean for actorName {}, service bean {}, processingStep {}", actorName, service, processingStep);
        return actorSystem.actorOf(ImportActor.props(service, processingStep, importActorHelper).withRouter(new SmallestMailboxPool(noOfInstances)), actorName + REF_SUFFIX);
    }

    @Override
    public ActorRef create(String actorName, Service service, String processingStep, int noOfInstances, String mailbox,
                           String dispatcher) {
        logger.debug("creating actorRef bean for actorName {}, service bean {}, processingStep {}", actorName, service, processingStep);
        Props props = ImportActor.props(service, processingStep, importActorHelper).withRouter(new SmallestMailboxPool(noOfInstances));
        props = appendAdditionalProps(props, mailbox, dispatcher);
        return actorSystem.actorOf(props, actorName + REF_SUFFIX);
    }

    @Override
    public ActorRef create(String actorName, Service service, String processingStep) {
        logger.debug("creating actorRef bean for actorName {}, service bean {}, processingStep {}", actorName, service, processingStep);
        return actorSystem.actorOf(ImportActor.props(service, processingStep, importActorHelper).withRouter(FromConfig.getInstance()), actorName + REF_SUFFIX);
    }

    @Override
    public ActorRef create(String actorName, Service service, String processingStep, String mailbox, String dispatcher) {
        logger.debug("creating actorRef bean for actorName {}, service bean {}, processingStep {}", actorName, service, processingStep);
        Props props = ImportActor.props(service, processingStep, importActorHelper).withRouter(FromConfig.getInstance());
        props = appendAdditionalProps(props, mailbox, dispatcher);
        return actorSystem.actorOf(props, actorName + REF_SUFFIX);
    }

    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    public void setActorSystem(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public ImportActorHelper getImportActorHelper() {
        return importActorHelper;
    }

    public void setImportActorHelper(ImportActorHelper importActorHelper) {
        this.importActorHelper = importActorHelper;
    }

    private Props appendAdditionalProps(Props props, String mailbox, String dispatcher) {
        if (mailbox != null) {
            props = props.withMailbox(mailbox);
        }
        if (dispatcher != null) {
            props = props.withDispatcher(dispatcher);
        }
        return props;
    }

}
