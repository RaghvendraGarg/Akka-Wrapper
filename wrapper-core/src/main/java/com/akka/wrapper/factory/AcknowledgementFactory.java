package com.akka.wrapper.factory;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import com.akka.wrapper.dto.Acknowledgement;
import com.rabbitmq.client.Channel;

/**
 * Created by gargr on 20/04/17.
 */
@Component
public class AcknowledgementFactory {

    Acknowledgement create(Message message, Channel channel) {
        if (channel == null || message == null) {
            return null;
        }
        MessageProperties messageProperties = message.getMessageProperties();
        if (messageProperties != null) {
            Acknowledgement acknowledgement = new Acknowledgement(channel, messageProperties.getDeliveryTag());
            return acknowledgement;
        }
        return null;
    }

}
