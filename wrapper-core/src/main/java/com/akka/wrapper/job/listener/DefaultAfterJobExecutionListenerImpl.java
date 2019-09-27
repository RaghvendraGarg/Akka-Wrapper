package com.akka.wrapper.job.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by gargr on 03/03/17.
 */
@Component
public final class DefaultAfterJobExecutionListenerImpl implements AfterJobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAfterJobExecutionListenerImpl.class);

    @Override
    public void onFailure(AfterJobExecutionListenerContext j) {
        logger.error("Job failed at step {}, status {}, due to {}, for messageId {}, time taken in ms {}", j.getProcessingStep(), j.getStatus(), j.getMessage(), getMessageIds(j.getObject()), j.getTimeTaken());
    }

    @Override
    public void onSuccess(AfterJobExecutionListenerContext j) {
        logger.info("Message processed successfully messageId {}, time taken in ms {}", getMessageIds(j.getObject()), j.getTimeTaken());
    }

}
