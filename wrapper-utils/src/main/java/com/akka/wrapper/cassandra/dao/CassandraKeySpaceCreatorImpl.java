package com.akka.wrapper.cassandra.dao;

import com.akka.wrapper.exception.DataStorageException;
import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.*;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by gargr on 31/01/17.
 */
public class CassandraKeySpaceCreatorImpl implements CassandraKeySpaceCreator {

    protected static final String CREATE_IMPORTS_KEYSPACE = "CREATE KEYSPACE IF NOT EXISTS %s with replication = {'class':'NetworkTopologyStrategy', '%s':3};";

    private static final String STATUS_CHECK_CQL = "SELECT count(*) AS tablecount from system.schema_columnfamilies WHERE keyspace_name='%s';";

    private static final int EXPECTED_NO_OF_TABLES = 1;

    private static final Logger logger = LoggerFactory.getLogger(CassandraTemplateImpl.class);

    @Value("${dmg.imports.cassandra.keyspace}")
    private String keySpace;

    @Value("${dmg.imports.cassandra.datacenter}")
    private String dataCenter;

    @Resource(name = "cassandraSessionFactoryImpl")
    private CassandraSessionFactory cassandraSessionFactory;

    @PostConstruct
    public void setup() {
        create();
    }

    @Override
    public void create() throws DataStorageException {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking exists Keyspace: " + keySpace + " in DataCenter: " + dataCenter);
        }
        try {
            if (CassandraTemplate.Status.BAD.equals(checkKeyspaceTableStatus())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating Keyspace " + keySpace + " in DataCenter: " + dataCenter);
                }
                Session session = cassandraSessionFactory.getSession();
                session.execute(String.format(CREATE_IMPORTS_KEYSPACE, keySpace, dataCenter));
                logger.info("{} keyspace created in DataCenter {}", keySpace, dataCenter);
                return;
            }
            logger.info("{} already exists in DataCenter {} ", keySpace, dataCenter);
        } catch (NoHostAvailableException e) {
            throw new DataStorageException("CREATE KEYSPACE failed due to cluster hosts.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (AuthenticationException e) {
            throw new DataStorageException("CREATE KEYSPACE failed on cluster authentication.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (IllegalArgumentException e) {
            throw new DataStorageException("CREATE KEYSPACE failed due to query params.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (InvalidTypeException e) {
            throw new DataStorageException("CREATE KEYSPACE failed due to query types.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (QueryValidationException e) {
            throw new DataStorageException("CREATE KEYSPACE failed due to query syntax.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (QueryExecutionException e) {
            throw new DataStorageException("CREATE KEYSPACE failed on query execution.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (DriverException e) {
            throw new DataStorageException("CREATE KEYSPACE failed with unknown driver exception.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        } catch (Exception e) {
            throw new DataStorageException("CREATE KEYSPACE failed with unknown exception.  datacenter: " + dataCenter + " keyspace name: " + keySpace, e);
        }
    }

    private CassandraTemplate.Status checkKeyspaceTableStatus() {
        CassandraTemplate.Status status = CassandraTemplate.Status.BAD;
        Session session = cassandraSessionFactory.getSession();
        PreparedStatement ps = session.prepare(String.format(STATUS_CHECK_CQL, keySpace));
        BoundStatement bs = ps.bind();
        ResultSet rs = session.execute(bs.setConsistencyLevel(ConsistencyLevel.LOCAL_ONE));

        long tableCount = 0;

        if (!rs.isExhausted()) {
            Row row = rs.iterator().next();
            tableCount = row.getLong("tablecount");
        }
        if (tableCount >= EXPECTED_NO_OF_TABLES) {
            if (logger.isDebugEnabled()) {
                logger.debug("Status Check passed.");
            }
            status = CassandraTemplate.Status.GOOD;
        } else {
            logger.error("Status Check failed with 0 table count from keyspace assets.");
        }
        return status;
    }

}
