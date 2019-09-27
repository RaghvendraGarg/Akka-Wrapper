package com.akka.wrapper.cache.rabbitmq.receiver;

import com.akka.wrapper.cache.message.CacheRefreshSpec;
import com.akka.wrapper.cache.refresh.CacheRefresher;
import com.akka.wrapper.jackson.JacksonJsonObjectMapper;
import java.io.IOException;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class CacheRefreshSpecReceiver implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshSpecReceiver.class);

    @Resource(name = "platformUtilsjacksonObjectMapper")
    public JacksonJsonObjectMapper objectMapper;

    @Resource
    public CacheRefresher cacheRefresher;

    @Override
    public void onMessage(Message message) {
        try {
            byte[] body = message.getBody();
            CacheRefreshSpec cacheRefreshSpec = objectMapper.readValue(body, CacheRefreshSpec.class);
            cacheRefresher.refresh(cacheRefreshSpec);
        } catch (IOException e) {
            logger.error("Unable to convert json into CacheRefreshSpec", e);
        }
    }

}
