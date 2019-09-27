package com.akka.wrapper.cache.rabbitmq;

import com.akka.wrapper.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(Cache.class)
public class RabbitMqProperties {

    @Value("${vehicle.amqp.user}")
    private String user;

    @Value("${vehicle.amqp.password}")
    private String password;

    @Value("${vehicle.amqp.vhost}")
    private String vhost;

    @Value("${vehicle.amqp.channelCacheSize}")
    private Integer channelCacheSize;

    @Value("${vehicle.amqp.connectionTimeOut}")
    private Integer connectionTimeOut;

    @Value("${vehicle.amqp.requestedHeartbeat}")
    private Integer requestedHeartbeat;

    @Value("${con.inv.msg.address}")
    private String consumerAddress;

    @Value("${pub.inv.msg.address}")
    private String publisherAddress;

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getVhost() {
        return vhost;
    }

    public void setVhost(final String vhost) {
        this.vhost = vhost;
    }

    public Integer getChannelCacheSize() {
        return channelCacheSize;
    }

    public void setChannelCacheSize(final Integer channelCacheSize) {
        this.channelCacheSize = channelCacheSize;
    }

    public Integer getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(final Integer connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public Integer getRequestedHeartbeat() {
        return requestedHeartbeat;
    }

    public void setRequestedHeartbeat(final Integer requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }

    public void setConsumerAddress(String consumerAddress) {
        this.consumerAddress = consumerAddress;
    }

    public String getPublisherAddress() {
        return publisherAddress;
    }

    public void setPublisherAddress(String publisherAddress) {
        this.publisherAddress = publisherAddress;
    }
}
