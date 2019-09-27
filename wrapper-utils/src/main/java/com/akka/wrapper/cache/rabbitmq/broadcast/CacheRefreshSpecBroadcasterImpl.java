package com.akka.wrapper.cache.rabbitmq.broadcast;

import com.akka.wrapper.cache.CacheRefreshSpecFactory;
import com.akka.wrapper.cache.message.CacheRefreshSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;

public class CacheRefreshSpecBroadcasterImpl implements CacheRefreshSpecBroadcaster {

    @Resource(name = "rabbitTemplateForPublisher")
    private AmqpTemplate amqpTemplate;

    @Resource(name = "platformUtilsjacksonObjectMapper")
    public ObjectMapper objectMapper;

    @Resource
    public CacheRefreshSpecFactory cacheRefreshSpecFactory;

    @Override
    public void broadcast(CacheRefreshSpec spec) throws Exception {
        if (spec == null) {
            return;
        }
        String message = objectMapper.writeValueAsString(spec);
        amqpTemplate.convertAndSend(message);
    }

    /**
     * attributeValues should be in same order as possibleKeyNames in @{@link com.akka.wrapper.cache.Cache} constructor for e.g.
     * possibleKeyNames is (source, inventoryOwner) then attributeValues should be (Homenet, gmps-21) where Homenet is a source name and gmps-21 is an inventoryOwner
     *
     * @param cacheName
     * @param attributeValues
     */
    @Override
    public void broadcast(String cacheName, String... attributeValues) throws Exception {
        CacheRefreshSpec cacheRefreshSpec = cacheRefreshSpecFactory.create(cacheName, attributeValues);
        broadcast(cacheRefreshSpec);
    }
}
