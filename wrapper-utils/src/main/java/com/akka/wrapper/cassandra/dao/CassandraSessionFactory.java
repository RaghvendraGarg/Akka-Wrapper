package com.akka.wrapper.cassandra.dao;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

/**
 * Session Factory for a Cassandra cluster.
 * 
 * It provides sessions to communicate with the cluster. Typical clients should not attempt to close the session, as
 * specific implementations may attempt to maintain a connection pool in order to manage resources.
 * 
 * @author chitav
 */
public interface CassandraSessionFactory {
    /**
     * Establish a session to the Cassandra cluster.
     * 
     * There is no need for connection pooling, as the session handles connection pooling and load balancing to all
     * (reachable) hosts.
     */
    public Session getSession() throws NoHostAvailableException;
}
