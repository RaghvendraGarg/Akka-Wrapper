package com.akka.wrapper.cache.rabbitmq.broadcast;

import com.akka.wrapper.cache.message.CacheRefreshSpec;

public interface CacheRefreshSpecBroadcaster {

    void broadcast(CacheRefreshSpec spec) throws Exception;

    void broadcast(String cacheName, String... attributeValues) throws Exception;

}
