package com.akka.wrapper.cassandra.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.datastax.driver.core.Cluster;

public class CassandraSessionFactoryImplTest {

    private CassandraSessionFactoryImpl cassandraSessionFactory = new CassandraSessionFactoryImpl();

    @Mock
    Cluster.Builder clusterBuilder;

    @Mock
    private Cluster cluster;

    Set<String> hosts;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        hosts = new HashSet<>();
        hosts.add("seaddmgdst01.dev-sea.cobaltgroup.com");
        doReturn(clusterBuilder).when(clusterBuilder).addContactPoint(any());
        doReturn(clusterBuilder).when(clusterBuilder).withCredentials(any(), any());
    }

    @Test
    public void initializeTest() {

        ReflectionTestUtils.setField(cassandraSessionFactory, "hosts", hosts);
        ReflectionTestUtils.setField(cassandraSessionFactory, "username", "cassandra");
        ReflectionTestUtils.setField(cassandraSessionFactory, "password", "cassandra");
        cassandraSessionFactory.initialize();
        Assert.assertNotNull(cassandraSessionFactory.getSession());
        cassandraSessionFactory.cleanup();
    }

    @Test
    public void testCleanup() {
        ReflectionTestUtils.setField(cassandraSessionFactory, "hosts", hosts);
        ReflectionTestUtils.setField(cassandraSessionFactory, "username", "cassandra");
        ReflectionTestUtils.setField(cassandraSessionFactory, "password", "cassandra");
        boolean isExceptionOccurred = false;
        cassandraSessionFactory.initialize();
        try {
            cassandraSessionFactory.cleanup();
        } catch (Exception e) {
            isExceptionOccurred = true;
        }

        Assert.assertFalse(isExceptionOccurred);
    }

    @Test(expected = IllegalStateException.class)
    public void initializeWithoutHostTest() {
        ReflectionTestUtils.setField(cassandraSessionFactory, "hosts", new HashSet<String>());
        cassandraSessionFactory.initialize();
    }

}