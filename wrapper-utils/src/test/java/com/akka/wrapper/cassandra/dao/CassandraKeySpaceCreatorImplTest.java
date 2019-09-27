package com.akka.wrapper.cassandra.dao;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraKeySpaceCreatorImplTest {

    @Mock
    private CassandraSessionFactory cassandraSessionFactory;

    @InjectMocks
    private CassandraKeySpaceCreatorImpl cassandraKeySpaceCreator;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private BoundStatement boundStatement;

    @Mock
    private Session session;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        Mockito.when(cassandraSessionFactory.getSession()).thenReturn(session);
        when(session.prepare(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.bind()).thenReturn(boundStatement);
    }

    @Test
    public void createWhenStatusIsGood() throws Exception{
        Mockito.doReturn(buildMockResultSet(2L)).when(session).execute(Mockito.any(BoundStatement.class));
        cassandraKeySpaceCreator.create();
        Mockito.verify(cassandraSessionFactory, times(1)).getSession();
    }

    @Test
    public void createWhenStatusIsBad() throws Exception{
        Mockito.doReturn(buildMockResultSet(0L)).when(session).execute(Mockito.any(BoundStatement.class));
        cassandraKeySpaceCreator.create();
        Mockito.verify(cassandraSessionFactory, times(2)).getSession();
        verify(session).execute(String.format(CassandraKeySpaceCreatorImpl.CREATE_IMPORTS_KEYSPACE, null, null));
    }


    private ResultSet buildMockResultSet(Long count) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        CassandraTemplateImplTest.<Row>mockIterable(resultSet, buildMockRow(count));
        return resultSet;
    }

    private Row buildMockRow(Long count) throws Exception {
        Row row = mock(Row.class);
        when(row.getLong("tablecount")).thenReturn(count);
        return row;
    }
}