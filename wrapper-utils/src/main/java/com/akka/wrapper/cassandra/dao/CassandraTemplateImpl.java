package com.akka.wrapper.cassandra.dao;

import com.akka.wrapper.exception.DataStorageException;
import com.akka.wrapper.jackson.JacksonJsonObjectMapper;
import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;
import java.io.IOException;
import java.util.*;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public class CassandraTemplateImpl<T> implements CassandraTemplate<T> {

    private static final Logger logger = LoggerFactory.getLogger(CassandraTemplateImpl.class);

    // See comments in the prepareStatement method for details about why this map is useful
    private Map<String, PreparedStatement> preparedStatementsMap = new HashMap<>();

    @Value("${dmg.imports.cassandra.keyspace}")
    private String keySpace;

    @Resource(name = "cassandraSessionFactoryImpl")
    private CassandraSessionFactory cassandraSessionFactory;

    @Resource
    private JacksonJsonObjectMapper objectMapper;

    public void createCassandraTable(String tableName, String query) {
        try {
            String format = String.format(query, keySpace);
            logger.info("creating cassandra table {} for key space {} and query {} if not already present ", tableName , keySpace, format );
            Session session = cassandraSessionFactory.getSession();
            session.execute(format);
        } catch (NoHostAvailableException e) {
            throw new DataStorageException("CREATE TABLE failed due to cluster hosts.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (AuthenticationException e) {
            throw new DataStorageException("CREATE TABLE failed on cluster authentication.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("CREATE TABLE failed due to query params.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (InvalidTypeException e) {
            throw new DataStorageException("CREATE TABLE failed due to query types.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (QueryValidationException e) {
            throw new DataStorageException("CREATE TABLE failed due to query syntax.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (QueryExecutionException e) {
            throw new DataStorageException("CREATE TABLE failed on query execution.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (DriverException e) {
            throw new DataStorageException("CREATE TABLE failed with unknown driver exception.  table: " + tableName + " keyspace name: " + keySpace, e);
        } catch (Exception e) {
            throw new DataStorageException("CREATE TABLE failed with unknown exception.  table: " + tableName + " keyspace name: " + keySpace, e);
        }

    }

    /**
     * Insert/Update
     *
     * @param query
     * @param consistencyLevel
     * @param params
     */
    @Override
    @Retryable(include = ReadTimeoutException.class, maxAttempts = 3, backoff = @Backoff(value = 2000))
    public void upsert(String query, ConsistencyLevel consistencyLevel, String... params) {
        try {
            bindAndExecute(String.format(query, keySpace), consistencyLevel, params);
        } catch (NoHostAvailableException e) {
            throw new DataStorageException("SAVE failed due to cluster hosts, params = " + StringUtils.join(params, " ,"), e);
        } catch (AuthenticationException e) {
            throw new DataStorageException("SAVE failed on cluster authentication, params = " + StringUtils.join(params, " ,"), e);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("SAVE failed due to query params, params = " + StringUtils.join(params, " ,"), e);
        } catch (InvalidTypeException e) {
            throw new DataStorageException("SAVE failed due to query types, params = " + StringUtils.join(params, " ,"), e);
        } catch (QueryValidationException e) {
            throw new DataStorageException("SAVE failed due to query syntax, params = " + StringUtils.join(params, " ,"), e);
        } catch (QueryExecutionException e) {
            throw new DataStorageException("SAVE failed on query execution, params = " + StringUtils.join(params, " ,"), e);
        } catch (DriverException e) {
            throw new DataStorageException("SAVE failed with unknown driver, params = " + StringUtils.join(params, " ,"), e);
        } catch (Exception e) {
            throw new DataStorageException("SAVE failed with unknown exception, params = " + StringUtils.join(params, " ,"), e);
        }
    }

    /**
     * returns an object of type T , By default this will read the first column from the resultset and try to convert that
     * JSON into T
     *
     * @param query
     * @param params
     * @return
     */
    @Override
    @Retryable(include = ReadTimeoutException.class, maxAttempts = 3, backoff = @Backoff(value = 2000))
    public T fetch(String query, ConsistencyLevel consistencyLevel, Class<T> t, String... params) {
        try {
            ResultSet rs = fetch(query, consistencyLevel, params);
            return convert(rs, t);
        } catch (IOException e) {
            throw new DataStorageException("FIND failed with unknown exception, params = " + StringUtils.join(params, " ,"), e);
        }
    }

    @Override
    public List<T> fetchAll(String query, ConsistencyLevel consistencyLevel, Class<T> t, String... params) {
        try {
            ResultSet rs = fetch(query, consistencyLevel, params);
            return convertAll(rs, t);
        } catch (Exception e) {
            throw new DataStorageException("FIND failed with unknown exception, params = " + StringUtils.join(params, " ,"), e);
        }
    }

    @Override
    public ResultSet fetchAll(String query, ConsistencyLevel consistencyLevel, String... params) {
        return fetch(query, consistencyLevel, params);
    }

    @Override
    @Retryable(include = ReadTimeoutException.class, maxAttempts = 3, backoff = @Backoff(value = 2000))
    public ResultSet fetch(String query, ConsistencyLevel consistencyLevel, String... params) {
        try {
            return bindAndExecute(String.format(query, keySpace), consistencyLevel, params);
        } catch (NoHostAvailableException e) {
            throw new DataStorageException("FIND failed due to cluster hosts, params = " + StringUtils.join(params, " ,"), e);
        } catch (AuthenticationException e) {
            throw new DataStorageException("FIND failed on cluster authentication, params = " + StringUtils.join(params, " ,"), e);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("FIND failed due to query params, params = " + StringUtils.join(params, " ,"), e);
        } catch (InvalidTypeException e) {
            throw new DataStorageException("FIND failed due to query types, params = " + StringUtils.join(params, " ,"), e);
        } catch (QueryValidationException e) {
            throw new DataStorageException("FIND failed due to query syntax, params = " + StringUtils.join(params, " ,"), e);
        } catch (QueryExecutionException e) {
            throw new DataStorageException("FIND failed on query execution, params = " + StringUtils.join(params, " ,"), e);
        } catch (DriverException e) {
            throw new DataStorageException("FIND failed with unknown driver, params = " + StringUtils.join(params, " ,"), e);
        } catch (Exception e) {
            throw new DataStorageException("FIND failed with unknown exception, params = " + StringUtils.join(params, " ,"), e);
        }
    }

    @Override
    @Retryable(include = ReadTimeoutException.class, maxAttempts = 3, backoff = @Backoff(value = 2000))
    public void delete(String query, ConsistencyLevel consistencyLevel, String... params) {
        try {
            bindAndExecute(String.format(query, keySpace), DELETE_CONSISTENCY_LEVEL, params);
        } catch (NoHostAvailableException e) {
            throw new DataStorageException("DELETE failed due to cluster hosts, params = " + StringUtils.join(params, " ,"), e);
        } catch (AuthenticationException e) {
            throw new DataStorageException("DELETE failed on cluster authentication, params = " + StringUtils.join(params, " ,"), e);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("DELETE failed due to query params, params = " + StringUtils.join(params, " ,"), e);
        } catch (InvalidTypeException e) {
            throw new DataStorageException("DELETE failed due to query types, params = " + StringUtils.join(params, " ,"), e);
        } catch (QueryValidationException e) {
            throw new DataStorageException("DELETE failed due to query syntax, params = " + StringUtils.join(params, " ,"), e);
        } catch (QueryExecutionException e) {
            throw new DataStorageException("DELETE failed on query execution, params = " + StringUtils.join(params, " ,"), e);
        } catch (DriverException e) {
            throw new DataStorageException("DELETE failed with unknown driver, params = " + StringUtils.join(params, " ,"), e);
        } catch (Exception e) {
            throw new DataStorageException("DELETE failed with unknown exception, params = " + StringUtils.join(params, " ,"), e);
        }
    }

    private T convert(ResultSet rs, Class<T> t) throws IOException {
        Iterator<Row> iterator = rs.iterator();
        if (iterator.hasNext()) {
            Row row = iterator.next();
            String json = row.getString(0);
            return StringUtils.isBlank(json) ? null : objectMapper.readValue(json, t);
        }
        return null;
    }

    private List<T> convertAll(ResultSet rs, Class<T> t) throws IOException {
        List<T> lst = new ArrayList<T>();
        Iterator<Row> iterator = rs.iterator();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            String json = row.getString(0);
            if (!StringUtils.isBlank(json)) {
                T readValue = objectMapper.readValue(json, t);
                lst.add(readValue);
            }
        }
        return lst;
    }

    public JacksonJsonObjectMapper getObjectMapper() {
        return objectMapper;
    }

    ResultSet bindAndExecute(String cql, ConsistencyLevel consistencyLevel, Object... params) {
        Session session = cassandraSessionFactory.getSession();
        if (params != null && params.length > 0) {
            PreparedStatement ps = prepareStatement(session, cql);
            BoundStatement bs = ps.bind(params);
            return session.execute(bs.setConsistencyLevel(consistencyLevel));
        }
        return session.execute(cql);
    }

    private PreparedStatement prepareStatement(Session session, String query) {
        // Prepared statements require a session to create, but can be bound and executed in another
        // - prepared statements in Cassandra are stored on the cluster
        // - as long as the cluster connection is up, we can cache and reuse prepared statements
        // - currently CassandraSessionFactoryImpl makes the connection once, at creation time
        // NOTE: if the above conditions change, the caching data and strategy may need to change
        PreparedStatement ps = preparedStatementsMap.get(query);

        if (ps == null) {
            ps = session.prepare(query);
            preparedStatementsMap.put(query, ps);
        }

        return ps;
    }

    void setCassandraSessionFactory(CassandraSessionFactory cassandraSessionFactory) {
        this.cassandraSessionFactory = cassandraSessionFactory;
    }

    void setObjectMapper(JacksonJsonObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
