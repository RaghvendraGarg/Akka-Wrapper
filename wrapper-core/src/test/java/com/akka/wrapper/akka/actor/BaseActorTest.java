package com.akka.wrapper.akka.actor;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;

public abstract class BaseActorTest extends TestKit {

    public static ActorSystem _system = ActorSystem.create("TestSys");

    protected TestActorRef testActorRef;

    public BaseActorTest(Class actorClass) {
        super(_system);
        testActorRef = TestActorRef.create(_system, Props.create(actorClass));
    }

    protected TestActorRef getTestActorRef() {
        return testActorRef;
    }

    protected Actor getActor() {
        return testActorRef.underlyingActor();
    }

}
