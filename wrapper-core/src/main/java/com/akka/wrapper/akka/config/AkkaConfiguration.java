package com.akka.wrapper.akka.config;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.akka.wrapper.akka.spring.SpringExtension;
import com.akka.wrapper.contants.ImportContants;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.SmallestMailboxPool;

/**
 * Created by gargr on 10/02/17.
 */
@Configuration
public class AkkaConfiguration {

    @Value("#{'${import.actor.system.name:ImportSystem}'}")
    private String actorSystemName;

    @Resource
    private ApplicationContext applicationContext;

    @Bean(name = ImportContants.AKKA_ACTOR_SYSTEM_BEAN_NAME)
    public ActorSystem actorSystem() {
        final ActorSystem system = ActorSystem.create(actorSystemName);
        SpringExtension.SpringExtProvider.get(system).initialize(applicationContext);
        return system;
    }

    @Bean
    public ActorRef jobInitiatorActorRef(@Qualifier(value = "actorSystem") ActorSystem actorSystem) {
        ActorRef actorRef = actorSystem.actorOf(SpringExtension.SpringExtProvider.get(actorSystem).props("jobInitiatorActor").withRouter(new SmallestMailboxPool(10)), "jobInitiatorActor");
        return actorRef;
    }

    @Bean
    public ActorRef splittingActorRef(@Qualifier(value = "actorSystem") ActorSystem actorSystem) {
        ActorRef actorRef = actorSystem.actorOf(SpringExtension.SpringExtProvider.get(actorSystem).props("splittingActor").withRouter(new SmallestMailboxPool(5)), "splittingActor");
        return actorRef;
    }
}
