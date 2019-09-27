package com.akka.wrapper.dto;

import com.rabbitmq.client.Channel;

public class Acknowledgement {

    private Channel channel;

    private Long deliveryTag;

    private long startTime;

    public Acknowledgement(Channel channel, Long deliveryTag) {
        this.channel = channel;
        this.deliveryTag = deliveryTag;
        startTime = System.currentTimeMillis();
    }

    public long getStartTime() {
        return startTime;
    }

    public Channel getChannel() {
        return channel;
    }

    public Long getDeliveryTag() {
        return deliveryTag;
    }

}
