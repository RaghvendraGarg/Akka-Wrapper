package com.akka.wrapper.cache.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.akka.wrapper.cache.Cache;
import com.akka.wrapper.cache.rabbitmq.broadcast.CacheRefreshSpecBroadcaster;
import com.akka.wrapper.cache.rabbitmq.broadcast.CacheRefreshSpecBroadcasterImpl;
import com.akka.wrapper.cache.rabbitmq.receiver.CacheRefreshSpecReceiver;
import com.akka.wrapper.cache.refresh.CacheRefresher;
import com.akka.wrapper.cache.refresh.CacheRefresherImpl;

@Configuration
@ConditionalOnBean(Cache.class)
public class CacheRefreshConfiguration {

    public static final String FANOUT_EXCHANGE_FOR_CACHE_REFRESH = "imports.cacheRefreshFanoutExchange";

    private static final int MESSAGE_EXPIRE_TTL = 60000;

    private static final String MESSAGE_TTL_KEY = "x-message-ttl";

    @Resource
    public RabbitMqProperties rabbitMqProperties;

    @Resource
    public QueueNameUtil queueNameUtil;

    @Resource
    public CacheRefreshSpecReceiver cacheRefreshSpecReceiver;

    @Bean(name = "connectionFactoryForPublisher")
    @Primary
    public ConnectionFactory connectionFactory() {
        return connectionFactory(rabbitMqProperties, rabbitMqProperties.getPublisherAddress());
    }

    @Bean(name = "connectionFactoryForConsumer")
    public ConnectionFactory connectionFactoryForBatchStatus() {
        return connectionFactory(rabbitMqProperties, rabbitMqProperties.getConsumerAddress());
    }

    private ConnectionFactory connectionFactory(RabbitMqProperties rabbitCredential, String address) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(address);
        connectionFactory.setVirtualHost(rabbitCredential.getVhost());
        connectionFactory.setUsername(rabbitCredential.getUser());
        connectionFactory.setPassword(rabbitCredential.getPassword());
        connectionFactory.setChannelCacheSize(rabbitCredential.getChannelCacheSize());
        connectionFactory.setConnectionTimeout(rabbitCredential.getConnectionTimeOut());
        connectionFactory.setRequestedHeartBeat(rabbitCredential.getRequestedHeartbeat());
        return connectionFactory;
    }


    @Bean
    public FanoutExchange cacheRefreshFanoutExchange() {
        return new FanoutExchange(FANOUT_EXCHANGE_FOR_CACHE_REFRESH, true, false);
    }

    @Bean
    public Queue cacheRefreshQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put(MESSAGE_TTL_KEY, MESSAGE_EXPIRE_TTL);
        return new Queue(queueNameUtil.getQueueName(), true, false, false, args);
    }

    @Bean
    public Binding cacheRefreshbatchMessageBinding(@Qualifier("cacheRefreshFanoutExchange") FanoutExchange fanoutExchange, @Qualifier("cacheRefreshQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }

    @Bean(name = "rabbitAdminForPublisher")
    public RabbitAdmin rabbitAdminForPublisher(@Qualifier(value = "connectionFactoryForPublisher") ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        return rabbitAdmin;
    }

    @Bean(name = "rabbitAdminForConsumer")
    public RabbitAdmin rabbitAdminForConsumer(@Qualifier(value = "connectionFactoryForConsumer") ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        return rabbitAdmin;
    }

    @Bean(name = "rabbitTemplateForPublisher")
    public RabbitTemplate rabbitTemplateForPublisher(@Qualifier(value = "connectionFactoryForPublisher") ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange(FANOUT_EXCHANGE_FOR_CACHE_REFRESH);
        return template;
    }

    @Bean(name = "cachelistenerContainer")
    public SimpleMessageListenerContainer cachelistenerContainer(@Qualifier("connectionFactoryForConsumer") ConnectionFactory connectionFactory,
                                                                 @Qualifier("rabbitAdminForConsumer") RabbitAdmin rabbitAdminForConsumer) {
        final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueNameUtil.getQueueName());
        container.setMessageListener(cacheRefreshSpecReceiver);
        container.setShutdownTimeout(30);
        container.setRecoveryInterval(5);
        container.setRabbitAdmin(rabbitAdminForConsumer);
        container.setMissingQueuesFatal(false);
        return container;
    }

    @Bean
    public CacheRefresher cacheRefresherImpl(){
        CacheRefresherImpl cacheRefresher = new CacheRefresherImpl();
        return cacheRefresher;
    }

    @Bean
    CacheRefreshSpecBroadcaster cacheRefreshSpecBroadcasterImpl(){
        CacheRefreshSpecBroadcaster cacheRefreshSpecBroadcaster = new CacheRefreshSpecBroadcasterImpl();
        return cacheRefreshSpecBroadcaster;
    }

    @Bean
    public CacheRefreshSpecReceiver cacheRefreshSpecReceiver(){
        CacheRefreshSpecReceiver cacheRefreshSpecReceiver = new CacheRefreshSpecReceiver();
        return cacheRefreshSpecReceiver;
    }

}