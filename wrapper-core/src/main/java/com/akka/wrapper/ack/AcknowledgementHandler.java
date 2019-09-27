package com.akka.wrapper.ack;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import com.akka.wrapper.dto.Acknowledgement;
import com.akka.wrapper.dto.ImportMessage;
import com.akka.wrapper.dto.ImportProcessStatus;

/**
 * Created by gargr on 3/1/17.
 */
@Component
public class AcknowledgementHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void acknowledgeMessage(Object message) {
        if (message instanceof MultiValueMap) {
            MultiValueMap<String, ImportMessage> importMessages = (MultiValueMap<String, ImportMessage>) message;
            importMessages.entrySet().stream().
                    map(Entry::getValue).
                    flatMap(Collection::stream).
                    forEach(v -> acknowledgeMessage(v.getAcknowledgement(), v.getImportProcessStatus(), v.getLoggingDetails()));
        } else if (message instanceof ImportMessage) {
            ImportMessage importMessage = (ImportMessage) message;
            Acknowledgement acknowledgement = importMessage.getAcknowledgement();
            acknowledgeMessage(acknowledgement, importMessage.getImportProcessStatus(), importMessage.getLoggingDetails());
        }
    }

    private void acknowledgeMessage(Acknowledgement ackMessage, ImportProcessStatus processingStatus, String loggingDetails) {
        if (ackMessage.getChannel() != null) {
            try {
                switch (processingStatus) {
                    case SUCCESS:
                        logger.info("Final ack sent >>> Vehicle processed. ACK sent, time {}, {}", System.currentTimeMillis() - ackMessage.getStartTime(), loggingDetails);
                        ackMessage.getChannel().basicAck(ackMessage.getDeliveryTag(), false);
                        break;
                    case FAILURE_NON_RECOVERABLE:
                        logger.info("Final ack sent >>> Vehicle failed. NACK sent, time {}, {}", System.currentTimeMillis() - ackMessage.getStartTime(), loggingDetails);
                        ackMessage.getChannel().basicAck(ackMessage.getDeliveryTag(), false);
                        break;
                    case FAILURE_RECOVERABLE:
                        logger.info("Recoverable ack sent >>> Vehicle failed requeued for retry. NACK sent, time {}, {}", System.currentTimeMillis() - ackMessage.getStartTime(), loggingDetails);
                        ackMessage.getChannel().basicNack(ackMessage.getDeliveryTag(), false, true);
                        break;
                    case FAILURE_RECOVERABLE_WITH_DELAY:
                        logger.info("Recoverable ack sent >>> Vehicle failed requeued for delayed retry, NACK sent, time {}, {}", System.currentTimeMillis() - ackMessage.getStartTime(), loggingDetails);
                        ackMessage.getChannel().basicNack(ackMessage.getDeliveryTag(), false, false);
                        break;
                    case FAILED_DUE_TO_OPEN_CIRUICTBREAKER:
                        logger.info("Recoverable ack sent >>> Vehicle failed due to circuit breaker, requeued for delayed retry, NACK sent, time {}, {}", System.currentTimeMillis() - ackMessage.getStartTime(), loggingDetails);
                        ackMessage.getChannel().basicNack(ackMessage.getDeliveryTag(), false, false);
                        break;
                }
            } catch (IOException ex) {
                logger.error("exception occurred while acknowledging message {} " + loggingDetails, ex);
            }
        }
    }
}
