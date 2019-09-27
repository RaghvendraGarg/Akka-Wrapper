package com.akka.wrapper.factory;

import com.akka.wrapper.dto.Acknowledgement;
import com.akka.wrapper.dto.ImportMessage;
import com.rabbitmq.client.Channel;
import javax.annotation.Resource;
import org.springframework.amqp.core.Message;

/**
 * Created by gargr on 20/04/17.
 */
public abstract class ImportMessageQueueFactory {

    @Resource
    private AcknowledgementFactory acknowledgementFactory;

    public ImportMessage create(Message message, Channel channel) throws Exception {
        ImportMessage importMessage = createMessage(message);
        if (importMessage == null) {
            return null;
        }
        Acknowledgement acknowledgement = acknowledgementFactory.create(message, channel);
        importMessage.setAcknowledgement(acknowledgement);
        return importMessage;
    }

    protected abstract ImportMessage createMessage(Message message) throws Exception;

}
