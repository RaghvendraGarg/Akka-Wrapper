package com.akka.wrapper.akka.actor.buncher;

import static org.mockito.Mockito.verify;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.akka.wrapper.dto.ImportMessage;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * Created by gargr on 06/03/17.
 */
public class AccumulatorFactoryTest {

    @Mock
    private ActorSystem actorSystem;

    @Mock
    private ActorRef splittingActorRef;

    @InjectMocks
    private AccumulatorFactory accumulatorFactory;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createBuncher() {
        Function<ImportMessage, String> f = new Function<ImportMessage, String>() {
            @Override
            public String apply(ImportMessage message) {
                return null;
            }
        };
        ActorRef actorRef = accumulatorFactory.createAccumulator("TEST_BUNCHER", 10, 11, f, 1);
        verify(actorSystem).actorOf(Mockito.any(Props.class), Mockito.eq("TEST_BUNCHER"));
    }

    @Test
    public void getSplitterActorRef(){
        ActorRef splittingActorRef = accumulatorFactory.getSplittingActorRef();
        Assert.assertNotNull(splittingActorRef);
    }


}