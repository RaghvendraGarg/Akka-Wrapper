package com.akka.wrapper.cassandra.dao;

import java.net.InetAddress;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.codahale.metrics.Gauge;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Metrics;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

/**
 * Session Factory Implementation to a Cassandra cluster.
 *
 * This needs to be configured (typically as Spring beans) for connectivity: - nativePort (native_transport_port from
 * conf/cassandra.yml - default 9042) - hosts (used for cluster discovery, need not be all the hosts in the cluster)
 *
 * Note that a cluster can be discovered from a single endpoint/host, and sessions will be created on all cluster hosts,
 * according to the LoadBalancing Policy. Configuring multiple hosts is still recommended for cluster discovery, unless
 * the endpoint is configured as load balancer.
 *
 * @author chitav
 */
public class CassandraSessionFactoryImpl implements CassandraSessionFactory {

    private static final Logger logger = LoggerFactory.getLogger(CassandraSessionFactoryImpl.class);

    @Value("#{'${dmg.imports.cassandra.hosts}'.split(',')}")
    private Set<String> hosts;

    @Value("${dmg.imports.cassandra.port}")
    private int nativePort = ProtocolOptions.DEFAULT_PORT;

    @Value("${dmg.imports.cassandra.username}")
    private String username;

    @Value("${dmg.imports.cassandra.password}")
    private String password;

    private Cluster cluster;

    private Object lock = new Object();

    // Session is multi-threaded, so it will be shared
    private Session session;

    /**
     * Initialize the connection to the Cassandra cluster.
     *
     * This initialization needs to happen before communicating with the cluster.
     *
     * @throws IllegalArgumentException
     *             if no IP addresses can be found
     * @throws IllegalStateException
     *             if the cluster was not configured properly (like hosts missing)
     * @throws SecurityException
     *             if a security manager denies hosts resolution
     */
    @PostConstruct
    public void initialize() throws IllegalArgumentException, IllegalStateException, SecurityException {
        cluster = null;

        if (hosts == null || hosts.isEmpty()) {
            logger.error("Misconfigured Cassandra store bean: No hosts");
            throw new IllegalStateException("Misconfigured Cassandra store bean: No hosts");
        }

        String[] hostsArray = new String[hosts.size()];
        try {
            Builder clusterBuilder = Cluster.builder();
            clusterBuilder = clusterBuilder.withPort(nativePort);
            hostsArray = hosts.toArray(hostsArray);
            clusterBuilder = clusterBuilder.addContactPoints(hostsArray).withCredentials(username, password);
            cluster = clusterBuilder.build();
            session = createSession();
        } catch (IllegalArgumentException e) {
            logger.error("INITIALIZE failed, no IP addresses can be found", e);
            throw e;
        } catch (SecurityException e) {
            logger.error("INITIALIZE failed, resolving hosts was denied", e);
            throw e;
        }

        logClusterDetail();
    }

    /**
     * Cleanup the connection to the Cassandra cluster.
     *
     */
    @PreDestroy
    public void cleanup() {
        if (cluster == null) {
            return;
        }

        logger.info("Shutting down Cassandra cluster (it could take up to the maximum reconnect interval)");
        cluster.close();
    }

    /**
     * {@inheritDoc}
     *
     * There is no need to use a connection pool or retry logic, as the Cassandra driver has built-in logic
     * (configurable with Policies) for these.
     *
     * @return Cassandra Session to the specified cluster
     * @throws NoHostAvailableException
     *             if the cluster has not been initialized or there are no reachable hosts
     * @throws AuthenticationException
     *             if authentication failed on connecting to the initial cluster endpoints
     */
    @Override
    public Session getSession() throws NoHostAvailableException, AuthenticationException {
        if (session == null || session.isClosed()) {
            synchronized (lock) {
                if (session == null || session.isClosed()) {
                    session = createSession();
                }
            }
        }
        return session;
    }

    private Session createSession() throws NoHostAvailableException, AuthenticationException {
        session = cluster.connect();
        Metrics metrics = cluster.getMetrics();
        if (metrics != null) {
            Gauge<Integer> openConnectionsMetric = metrics.getOpenConnections();
            Integer numberOfConnections = openConnectionsMetric.getValue();
            logger.info("New session created, TOTAL: " + numberOfConnections);
        } else {
            logger.warn("Cluster metrics are not available");
            logger.info("New session created, TOTAL: unknown");
        }
        return session;
    }

    private void logClusterDetail() {
        Metadata metadata = cluster.getMetadata();
        logger.info("CLUSTER: " + cluster.getClusterName());
        for (Host host : metadata.getAllHosts()) {
            String dataCenter = host.getDatacenter();
            String rack = host.getRack();
            InetAddress hostAddress = host.getAddress();
            logger.info("DC: " + dataCenter + ", RACK: " + rack + ", HOST: " + hostAddress);
        }
    }

}
