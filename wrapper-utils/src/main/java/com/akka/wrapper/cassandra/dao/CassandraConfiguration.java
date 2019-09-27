package com.akka.wrapper.cassandra.dao;

import com.akka.wrapper.jackson.JacksonJsonObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
@ConditionalOnProperty(value = "dmg.imports.cassandra.hosts")
public class CassandraConfiguration {

    @Bean
    public CassandraSessionFactory cassandraSessionFactoryImpl() {
        CassandraSessionFactory cassandraSessionFactory = new CassandraSessionFactoryImpl();
        return cassandraSessionFactory;
    }

    @Bean
    public CassandraKeySpaceCreator cassandraKeySpaceCreator(CassandraSessionFactory cassandraSessionFactory) {
        CassandraKeySpaceCreatorImpl cassandraKeySpaceCreator = new CassandraKeySpaceCreatorImpl();
        return cassandraKeySpaceCreator;
    }

    @Bean
    @DependsOn("cassandraKeySpaceCreator")
    public CassandraTemplate cassandraTemplate(@Qualifier("platformUtilsjacksonObjectMapper") JacksonJsonObjectMapper jacksonJsonObjectMapper) {
        CassandraTemplate cassandraTemplate = new CassandraTemplateImpl();
        ((CassandraTemplateImpl) cassandraTemplate).setObjectMapper(jacksonJsonObjectMapper);
        return cassandraTemplate;
    }

}
