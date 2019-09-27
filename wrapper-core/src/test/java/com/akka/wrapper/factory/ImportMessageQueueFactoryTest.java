package com.akka.wrapper.factory;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.Message;
import org.springframework.test.util.ReflectionTestUtils;

import com.akka.wrapper.dto.Acknowledgement;
import com.akka.wrapper.dto.ImportMessage;
import com.rabbitmq.client.Channel;

/**
 * Created by gargr on 20/04/17.
 */
public class ImportMessageQueueFactoryTest {

    @Mock
    private AcknowledgementFactory acknowledgementFactory;

    private ImportMessageQueueFactory importMessageQueueFactory = new Test();

    private TestImportMessage testImportMessage ;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @Mock
    private Acknowledgement acknowledgement;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(importMessageQueueFactory, "acknowledgementFactory", acknowledgementFactory);
    }

    @org.junit.Test
    public void createWhenImportMessageIsNull() throws Exception{
        testImportMessage = null;
        ImportMessage importMessage = importMessageQueueFactory.create(message, channel);
        Assert.assertNull(importMessage);
        Mockito.verifyZeroInteractions(acknowledgementFactory);
    }

    @org.junit.Test
    public void create() throws Exception{
        testImportMessage = new TestImportMessage("source");
        Mockito.when(acknowledgementFactory.create(message, channel)).thenReturn(acknowledgement);
        ImportMessage importMessage = importMessageQueueFactory.create(message, channel);
        Assert.assertEquals(testImportMessage, importMessage);
        Assert.assertEquals(acknowledgement, importMessage.getAcknowledgement());
        Mockito.verify(acknowledgementFactory).create(message, channel);
    }


    class Test extends ImportMessageQueueFactory {

        @Override
        protected ImportMessage createMessage(Message message) {
            return testImportMessage;
        }
    }

    class TestImportMessage extends ImportMessage {

        public TestImportMessage(String source) {
            super(source);
        }
    }

}