package com.akka.wrapper.akka.actor;

import com.akka.wrapper.service.Service;

import akka.actor.ActorRef;

/**
 * Created by gargr on 10/02/17.
 */
public interface ActorFactory {

    public static final String REF_SUFFIX = "Ref";

    /**
     * Creates an ActorRef bean from {@link ImportActor} and sets {@link Service} as the business logic class.
     * ActorRef bean name is created by appending actor name wit REF_SUFFIX
     * This will create an Actor with {@link akka.routing.SmallestMailboxPool} and default dispatcher and mailbox
     * For this no router configuration is required in application.conf, if you want to override this then use {@link #create(String, Service, String)}
     *
     * @param actorName
     * @param service
     * @param processingStep
     * @param noOfInstances
     * @return bean name of the actor
     */
    ActorRef create(String actorName, Service service, String processingStep, int noOfInstances);

    /**
     * Creates an ActorRef bean from {@link ImportActor} and sets {@link Service} as the business logic class.
     * ActorRef bean name is created by appending actor name wit REF_SUFFIX
     * This will create an Actor with {@link akka.routing.SmallestMailboxPool}
     * For this no router configuration is required in application.conf, if you want to override this then use {@link #create(String, Service, String, String, String)}
     * If dispatcher is null, default dispatcher is used. Otherwise, the provided dispatcher name should be configured in application.conf
     * If mailbox is null, the associated dispatcher's default mailbox will be used. Otherwise, the provided mailbox name should be configured in application.conf
     *
     *
     * @param actorName
     * @param service
     * @param processingStep
     * @param noOfInstances
     * @param mailbox
     * @param dispatcher
     * @return bean name of the actor
     */
    ActorRef create(String actorName, Service service, String processingStep, int noOfInstances, String mailbox,
                    String dispatcher);

    /**-
     * Creates an ActorRef bean from {@link ImportActor} and sets {@link Service} as the business logic class.
     * ActorRef bean name is created by appending actor name wit REF_SUFFIX
     * You can configure the actor's router configurations in application.conf
     * While configuring use ActorRef name, for example if you named your actor "firstActor", then the configuration in application.conf should look like
     * /firstActorRef {
     * router = smallest-mailbox-pool
     * nr-of-instances = 2
     * }
     * Default dispatcher and mailbox will be used
     *
     * @param actorName
     * @param service
     * @param processingStep
     * @return bean name of the actor
     */
    ActorRef create(String actorName, Service service, String processingStep);

    /**-
     * Creates an ActorRef bean from {@link ImportActor} and sets {@link Service} as the business logic class.
     * ActorRef bean name is created by appending actor name wit REF_SUFFIX
     * You can configure the actor's router configurations in application.conf
     * While configuring use ActorRef name, for example if you named your actor "firstActor", then the configuration in application.conf should look like
     * /firstActorRef {
     * router = smallest-mailbox-pool
     * nr-of-instances = 2
     * }
     * If dispatcher is null, default dispatcher is used. Otherwise, the provided dispatcher name should be configured in application.conf
     * If mailbox is null, the associated dispatcher's default mailbox will be used. Otherwise, the provided mailbox name should be configured in application.conf
     *
     * @param actorName
     * @param service
     * @param processingStep
     * @param mailbox
     * @param dispatcher
     * @return bean name of the actor
     */
    ActorRef create(String actorName, Service service, String processingStep, String mailbox, String dispatcher);
}
