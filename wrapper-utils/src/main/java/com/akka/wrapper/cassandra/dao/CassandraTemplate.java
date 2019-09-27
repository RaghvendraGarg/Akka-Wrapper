package com.akka.wrapper.cassandra.dao;

import java.util.List;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;

public interface CassandraTemplate<T> {

    ConsistencyLevel WRITE_CONSISTENCY_LEVEL = ConsistencyLevel.LOCAL_QUORUM;

    ConsistencyLevel READ_CONSISTENCY_LEVEL = ConsistencyLevel.LOCAL_ONE;

    ConsistencyLevel DELETE_CONSISTENCY_LEVEL = ConsistencyLevel.LOCAL_QUORUM;

    enum Status {
        BAD, GOOD
    }

    void upsert(String query, ConsistencyLevel consistencyLevel, String... params);

    T fetch(String query, ConsistencyLevel consistencyLevel, Class<T> t, String... params);

    List<T> fetchAll(String query, ConsistencyLevel consistencyLevel, Class<T> t, String... params);

    ResultSet fetch(String query, ConsistencyLevel consistencyLevel, String... params);

    ResultSet fetchAll(String query, ConsistencyLevel consistencyLevel, String... params);

    void delete(String query, ConsistencyLevel consistencyLevel, String... params);

    void createCassandraTable(String tableName, String query);
    
}
