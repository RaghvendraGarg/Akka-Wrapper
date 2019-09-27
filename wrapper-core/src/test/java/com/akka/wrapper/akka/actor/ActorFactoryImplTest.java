package com.akka.wrapper.akka.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.akka.wrapper.service.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.FromConfig;
import akka.routing.SmallestMailboxPool;

/**
 * Created by gargr on 06/03/17.
 */
public class ActorFactoryImplTest {

    private static final String PROCESSING_STEP = "testStep";

    private static final String TEST_ACTOR = "testActor";

    private static final String MAILBOX = "some-mailbox";

    private static final String DISPATCHER = "some-dispatcher";

    @Mock
    private ActorSystem actorSystem;

    @Mock
    private ImportActorHelper importActorHelper;

    @InjectMocks
    private ActorFactoryImpl actorFactory;

    @Mock
    private ActorRef actorRef;

    private ArgumentCaptor<Props> props;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        props = ArgumentCaptor.forClass(Props.class);
        when(actorSystem.actorOf(Mockito.any(Props.class), Mockito.eq(TEST_ACTOR))).thenReturn(actorRef);
    }

    @Test
    public void createWithRouterFromApplicationConf() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof FromConfig);
        assertEquals("", props.getValue().deploy().mailbox());
        assertEquals("", props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterFromApplicationConfAndWithProvidedMailbox() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, MAILBOX, null);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof FromConfig);
        assertEquals(MAILBOX, props.getValue().deploy().mailbox());
        assertEquals("", props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterFromApplicationConfAndWithProvidedDispatcher() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, null, DISPATCHER);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof FromConfig);
        assertEquals("", props.getValue().deploy().mailbox());
        assertEquals(DISPATCHER, props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterFromApplicationConfAndWithProvidedMailboxAndDispatcher() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, MAILBOX, DISPATCHER);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof FromConfig);
        assertEquals(MAILBOX, props.getValue().deploy().mailbox());
        assertEquals(DISPATCHER, props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterAsSmallestMailboxPool() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, 10);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof SmallestMailboxPool);
        assertEquals("", props.getValue().deploy().mailbox());
        assertEquals("", props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterAsSmallestMailboxPoolAndWithProvidedMailbox() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, 10, MAILBOX, null);
        verify(actorSystem).actorOf(Mockito.any(Props.class), Mockito.eq(TEST_ACTOR+"Ref"));
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof SmallestMailboxPool);
        assertEquals(MAILBOX, props.getValue().deploy().mailbox());
        assertEquals("", props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterAsSmallestMailboxPoolAndWithProvidedDispatcher() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, 10, null, DISPATCHER);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof SmallestMailboxPool);
        assertEquals("", props.getValue().deploy().mailbox());
        assertEquals(DISPATCHER, props.getValue().deploy().dispatcher());
    }

    @Test
    public void createWithRouterAsSmallestMailboxPoolAndWithProvidedMailboxAndDispatcher() {
        MockService service = new MockService();
        actorFactory.create(TEST_ACTOR, service, PROCESSING_STEP, 10, MAILBOX, DISPATCHER);
        verify(actorSystem).actorOf(props.capture(), Mockito.eq(TEST_ACTOR+"Ref"));
        assertTrue(props.getValue().deploy().routerConfig() instanceof SmallestMailboxPool);
        assertEquals(MAILBOX, props.getValue().deploy().mailbox());
        assertEquals(DISPATCHER, props.getValue().deploy().dispatcher());
    }


    class MockService implements Service {
        @Override
        public void apply(Object o) {

        }
    }

}