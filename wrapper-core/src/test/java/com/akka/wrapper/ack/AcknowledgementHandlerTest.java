package com.akka.wrapper.ack;

import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.dto.Acknowledgement;
import com.akka.wrapper.dto.ImportMessage;
import com.akka.wrapper.dto.ImportProcessStatus;
import com.rabbitmq.client.Channel;

/**
 * Created by gargr on 06/03/17.
 */
public class AcknowledgementHandlerTest {

    @Mock
    private Acknowledgement acknowledgeMent;

    @Mock
    private Channel channel;

    @Mock
    private ImportMessage importMessage;

    @InjectMocks
    private AcknowledgementHandler acknowledgementHandlerImpl;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(acknowledgeMent.getChannel()).thenReturn(channel);
        when(acknowledgeMent.getDeliveryTag()).thenReturn(10L);
        when(importMessage.getAcknowledgement()).thenReturn(acknowledgeMent);
    }

    @Test
    public void acknowledgeMessageWhenStatusIsFailedDueToCircuitBreakerOpen() throws Exception {
        when(importMessage.getImportProcessStatus()).thenReturn(ImportProcessStatus.FAILED_DUE_TO_OPEN_CIRUICTBREAKER);
        acknowledgementHandlerImpl.acknowledgeMessage(importMessage);
        Mockito.verify(channel).basicNack(10L, false, false);
    }

    @Test
    public void acknowledgeMessageWhenStatusIsSuccess() throws Exception {
        when(importMessage.getImportProcessStatus()).thenReturn(ImportProcessStatus.SUCCESS);
        acknowledgementHandlerImpl.acknowledgeMessage(importMessage);
        Mockito.verify(channel).basicAck(10L, false);
    }

    @Test
    public void acknowledgeMessageWhenStatusIsFailureNonRecoverable() throws Exception {
        when(importMessage.getImportProcessStatus()).thenReturn(ImportProcessStatus.FAILURE_NON_RECOVERABLE);
        acknowledgementHandlerImpl.acknowledgeMessage(importMessage);
        Mockito.verify(channel).basicAck(10L, false);
    }

    @Test
    public void acknowledgeMessageWhenStatusIsFailureRecoverable() throws Exception {
        when(importMessage.getImportProcessStatus()).thenReturn(ImportProcessStatus.FAILURE_RECOVERABLE);
        acknowledgementHandlerImpl.acknowledgeMessage(importMessage);
        Mockito.verify(channel).basicNack(10L, false, true);
    }

    @Test
    public void acknowledgeMessageWhenStatusIsFailureRecoverableWithDelay() throws Exception {
        when(importMessage.getImportProcessStatus()).thenReturn(ImportProcessStatus.FAILURE_RECOVERABLE_WITH_DELAY);
        acknowledgementHandlerImpl.acknowledgeMessage(importMessage);
        Mockito.verify(channel).basicNack(10L, false, false);
    }

    @Test
    public void acknowledgeMessageWhenStatusIsSuccessForMultiValueMap() throws IOException {
        when(importMessage.getImportProcessStatus()).thenReturn(ImportProcessStatus.SUCCESS);
        MultiValueMap<String, ImportMessage> m = new LinkedMultiValueMap<>();
        m.add("wewewe", importMessage);
        m.add("1234", importMessage);
        m.add("4321", importMessage);
        acknowledgementHandlerImpl.acknowledgeMessage(m);
        Mockito.verify(channel, Mockito.times(3)).basicAck(10L, false);
    }

}