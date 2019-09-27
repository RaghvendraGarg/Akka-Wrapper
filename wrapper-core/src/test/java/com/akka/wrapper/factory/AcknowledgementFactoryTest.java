package com.akka.wrapper.factory;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import com.akka.wrapper.dto.Acknowledgement;
import com.rabbitmq.client.Channel;

/**
 * Created by gargr on 20/04/17.
 */
public class AcknowledgementFactoryTest {

    private AcknowledgementFactory acknowledgementFactory = new AcknowledgementFactory();

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createWhenMessageOrChannelIsNull() {
        Acknowledgement acknowledgement = acknowledgementFactory.create(null, channel);
        Assert.assertNull(acknowledgement);
        acknowledgement = acknowledgementFactory.create(message, null);
        Assert.assertNull(acknowledgement);
        acknowledgement = acknowledgementFactory.create(null, null);
        Assert.assertNull(acknowledgement);
    }

    @Test
    public void create(){
        MessageProperties messageProperties = Mockito.mock(MessageProperties.class);
        when(messageProperties.getDeliveryTag()).thenReturn(10L);
        when(message.getMessageProperties()).thenReturn(messageProperties);
        Acknowledgement acknowledgement = acknowledgementFactory.create(message, channel);
        Assert.assertEquals(channel, acknowledgement.getChannel());
        Assert.assertEquals(new Long(10), acknowledgement.getDeliveryTag());
        Assert.assertNotNull(acknowledgement.getStartTime());
    }

    @Test
    public void createWhenMessagePropertiesIsNull(){
        when(message.getMessageProperties()).thenReturn(null);
        Acknowledgement acknowledgement = acknowledgementFactory.create(message, channel);
        Assert.assertNull(acknowledgement);
    }
}