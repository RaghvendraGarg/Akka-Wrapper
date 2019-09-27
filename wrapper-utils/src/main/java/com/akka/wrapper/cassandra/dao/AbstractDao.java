package com.akka.wrapper.cassandra.dao;

import com.akka.wrapper.jackson.JacksonJsonObjectMapper;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by gargr on 01/02/17.
 */
public abstract class AbstractDao<T> {

    @Resource
    protected CassandraTemplate<T> cassandraTemplate;

    @Resource
    protected JacksonJsonObjectMapper jacksonJsonObjectMapper;

    @PostConstruct
    public void setup() {
        cassandraTemplate.createCassandraTable(getTableName(), getCreateTableQuery());
    }

    public abstract String getCreateTableQuery();

    public abstract String getTableName();


}
