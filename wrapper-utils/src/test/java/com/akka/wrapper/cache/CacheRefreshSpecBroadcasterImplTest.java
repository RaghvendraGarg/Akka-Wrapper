package com.akka.wrapper.cache;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.akka.wrapper.cache.message.CacheRefreshSpec;
import com.akka.wrapper.cache.rabbitmq.broadcast.CacheRefreshSpecBroadcasterImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CacheRefreshSpecBroadcasterImplTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @Mock
    public ObjectMapper objectMapper;

    @Mock
    public CacheRefreshSpec cacheRefreshSpec;

    @Mock
    public CacheRefreshSpecFactory cacheRefreshSpecFactory;

    @InjectMocks
    private CacheRefreshSpecBroadcasterImpl cacheRefreshSpecBroadcaster;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(cacheRefreshSpecBroadcaster, "cacheRefreshSpecFactory" ,cacheRefreshSpecFactory);
    }

    @Test
    public void broadcastWhenSpecIsNull() throws Exception{
        cacheRefreshSpecBroadcaster.broadcast(null);
        Mockito.verifyZeroInteractions(objectMapper);
        Mockito.verifyZeroInteractions(amqpTemplate);
    }

    @Test
    public void broadcast() throws Exception{
        Mockito.when(cacheRefreshSpecFactory.create(Mockito.anyString(), Mockito.anyString())).thenReturn(cacheRefreshSpec);
        cacheRefreshSpecBroadcaster.broadcast(cacheRefreshSpec);
        Mockito.verify(objectMapper).writeValueAsString(cacheRefreshSpec);
        Mockito.verify(amqpTemplate).convertAndSend(Mockito.anyString());
    }

    @Test
    public void broadcastWhenValues() throws Exception{
        Mockito.when(cacheRefreshSpecFactory.create("test", "1", "2")).thenReturn(cacheRefreshSpec);
        cacheRefreshSpecBroadcaster.broadcast("test", "1", "2");
        Mockito.verify(objectMapper).writeValueAsString(cacheRefreshSpec);
        Mockito.verify(amqpTemplate).convertAndSend(Mockito.anyString());
    }

}