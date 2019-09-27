package com.akka.wrapper.cassandra.dao;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import com.akka.wrapper.exception.DataStorageException;
import com.akka.wrapper.jackson.JacksonJsonObjectMapper;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.exceptions.AuthenticationException;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.ReadTimeoutException;

/**
 * Created by gargr on 31/01/17.
 */
public class CassandraTemplateImplTest {

    public static final String CREATE_QUERY = "CREATE TABLE IF NOT EXISTS %s.table (test_column_1 text,test_column_2 text, PRIMARY KEY (test_column_1)) ;";
    public static final String TABLE_NAME = "table";
    public static final String UPSERT_QUERY = "INSERT INTO %s.table (test_column_1, test_column_2) VALUES (? ,?)";
    public static final String FETCH_QUERY = "SELECT test_column_2 from %s.table where test_column_1 = ?";
    public static final String DELETE_QUERY = "DELETE from %s.table where test_column_1 = ?";
    public static final String TEST_COLUMN_1_DATA = "test_column_1_data";
    public static final String TEST_COLUMN_2_DATA = "test_column_2_data";
    public static final String IMPORTS = "imports";
    private static final String MESSAGE_FOR_NO_HOST_AVILABLE_EXCEPTION = "%s failed due to cluster hosts, params = %s";
    private static final String MESSAGE_FOR_AUTHENTICATION_EXCEPTION = "%s failed on cluster authentication, params = %s";
    private static final String MESSAGE_FOR_ILLEGAL_ARGUMENT_EXCEPTION = "%s failed due to query params, params = %s";
    private static final String MESSAGE_FOR_INVALID_TYPE_EXCEPTION = "%s failed due to query types, params = %s";
    private static final String MESSAGE_DRIVER_EXCEPTION = "%s failed with unknown driver, params = %s";
    private static final String MESSAGE_UNKNOWN_EXCEPTION = "%s failed with unknown exception, params = %s";
    private static final String MESSAGE_QUERY_VALIDATION_EXCEPTION = "%s failed due to query syntax, params = %s";
    private static final String MESSAGE_QUERY_EXCECUTION_EXCEPTION = "%s failed on query execution, params = %s";

    private ArgumentCaptor<String> createTableParam = ArgumentCaptor.forClass(String.class);

    ArgumentCaptor<String> cql;

    ArgumentCaptor<ConsistencyLevel> consistencyLevel;

    ArgumentCaptor<Object[]> params;

    @Mock
    private Map<String, PreparedStatement> preparedStatementsMap;

    @Mock
    private CassandraSessionFactory cassandraSessionFactory;

    @Spy
    private CassandraTemplateImpl<TestObject> cassandraDao;

    @Mock
    private Session session;

    private JacksonJsonObjectMapper objectMapper = new JacksonJsonObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(cassandraDao, "keySpace", IMPORTS);
        ReflectionTestUtils.setField(cassandraDao, "cassandraSessionFactory", cassandraSessionFactory);
        ReflectionTestUtils.setField(cassandraDao, "objectMapper", objectMapper);
        cql = ArgumentCaptor.forClass(String.class);
        consistencyLevel = ArgumentCaptor.forClass(ConsistencyLevel.class);
        params = ArgumentCaptor.forClass(Object[].class);
    }

    @Test
    public void createTable() {
        when(cassandraSessionFactory.getSession()).thenReturn(session);
        doReturn(null).when(session).execute(createTableParam.capture());
        cassandraDao.createCassandraTable(TABLE_NAME, CREATE_QUERY);
        Assert.assertEquals(String.format(CREATE_QUERY, IMPORTS), createTableParam.getValue());
    }

    @Test
    public void upsert() throws Exception {
        doReturn(buildMockResultSet()).when(cassandraDao).bindAndExecute(cql.capture(), consistencyLevel.capture(), params.capture());
        TestObject testObject = createTestObject();
        cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, testObject.getData(), testObject.getAnotherData());
        Assert.assertEquals(ConsistencyLevel.LOCAL_QUORUM, consistencyLevel.getValue());
        Assert.assertEquals(String.format(UPSERT_QUERY, IMPORTS), cql.getValue());
        Assert.assertEquals(TEST_COLUMN_1_DATA, params.getAllValues().get(0));
        Assert.assertEquals(TEST_COLUMN_2_DATA, params.getAllValues().get(1));
    }


    @Test
    public void fetch() throws Exception {
        doReturn(buildMockResultSet()).when(cassandraDao).bindAndExecute(cql.capture(), consistencyLevel.capture(), params.capture());
        TestObject fetch = (TestObject) cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);

        Assert.assertEquals(ConsistencyLevel.LOCAL_ONE, consistencyLevel.getValue());
        Assert.assertEquals(String.format(FETCH_QUERY, IMPORTS), cql.getValue());
        Assert.assertEquals(TEST_COLUMN_1_DATA, params.getAllValues().get(0));
        Assert.assertEquals(fetch.anotherData, TEST_COLUMN_2_DATA);
        Assert.assertEquals(fetch.data, TEST_COLUMN_1_DATA);
    }


    @Test
    public void delete() throws Exception {
        doReturn(buildMockResultSet()).when(cassandraDao).bindAndExecute(cql.capture(), consistencyLevel.capture(), params.capture());
        cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        Assert.assertEquals(ConsistencyLevel.LOCAL_QUORUM, consistencyLevel.getValue());
        Assert.assertEquals(String.format(DELETE_QUERY, IMPORTS), cql.getValue());
    }

    @Test(expected = DataStorageException.class)
    public void checkNoHostAvailableExceptionWhileUpsert() throws Exception {
        throwNoHostAvailableException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_NO_HOST_AVILABLE_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkAuthenticationExceptionWhileUpsert() throws Exception {
        throwAuthenticationException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_AUTHENTICATION_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkIllegalArgumentExceptionWhileUpsert() throws Exception {
        throwIllegalArgumentException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_ILLEGAL_ARGUMENT_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkInvalidTypeExceptionWhileUpsert() throws Exception {
        throwInvalidTypeException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_INVALID_TYPE_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkDriverExceptionWhileUpsert() throws Exception {
        throwDriverException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_DRIVER_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkUnknownExceptionWhileUpsert() throws Exception {
        throwUnknownException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_UNKNOWN_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkQueryValidationExceptionWhileUpsert() throws Exception {
        throwQueryValidationException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_QUERY_VALIDATION_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkQueryExecutionExceptionInUpsert() throws Exception {
        throwQueryExecutionException(cassandraDao);
        try {
            cassandraDao.upsert(UPSERT_QUERY, CassandraTemplate.WRITE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_QUERY_EXCECUTION_EXCEPTION, "SAVE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkNoHostAvailableExceptionWhileDelete() throws Exception {
        throwNoHostAvailableException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_NO_HOST_AVILABLE_EXCEPTION, "DELETE", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkAuthenticationExceptionWhileDelete() throws Exception {
        throwAuthenticationException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_AUTHENTICATION_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkIllegalArgumentExceptionWhileDelete() throws Exception {
        throwIllegalArgumentException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_ILLEGAL_ARGUMENT_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkInvalidTypeExceptionWhileDelete() throws Exception {
        throwInvalidTypeException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_INVALID_TYPE_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkDriverExceptionWhileDelete() throws Exception {
        throwDriverException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_DRIVER_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkUnknownExceptionWhileDelete() throws Exception {
        throwUnknownException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_UNKNOWN_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkQueryValidationExceptionWhileDelete() throws Exception {
        throwQueryValidationException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_QUERY_VALIDATION_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkQueryExecutionExceptionWhileDelete() throws Exception {
        throwQueryExecutionException(cassandraDao);
        try {
            cassandraDao.delete(DELETE_QUERY, CassandraTemplate.DELETE_CONSISTENCY_LEVEL, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_QUERY_EXCECUTION_EXCEPTION, "DELETE",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    //


    @Test(expected = DataStorageException.class)
    public void checkNoHostAvailableExceptionWhileFetch() throws Exception {
        throwNoHostAvailableException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_NO_HOST_AVILABLE_EXCEPTION, "FIND", TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkAuthenticationExceptionWhileFetch() throws Exception {
        throwAuthenticationException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_AUTHENTICATION_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkIllegalArgumentExceptionWhileFetch() throws Exception {
        throwIllegalArgumentException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_ILLEGAL_ARGUMENT_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkInvalidTypeExceptionWhileFetch() throws Exception {
        throwInvalidTypeException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_FOR_INVALID_TYPE_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkDriverExceptionWhileFetch() throws Exception {
        throwDriverException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_DRIVER_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkUnknownExceptionWhileFetch() throws Exception {
        throwUnknownException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_UNKNOWN_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkQueryValidationExceptionWhileFetch() throws Exception {
        throwQueryValidationException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_QUERY_VALIDATION_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    @Test(expected = DataStorageException.class)
    public void checkQueryExecutionExceptionWhileFetch() throws Exception {
        throwQueryExecutionException(cassandraDao);
        try {
            cassandraDao.fetch(FETCH_QUERY, CassandraTemplate.READ_CONSISTENCY_LEVEL, TestObject.class, TEST_COLUMN_1_DATA);
        } catch (Exception e) {
            Assert.assertEquals(String.format(MESSAGE_QUERY_EXCECUTION_EXCEPTION, "FIND",TEST_COLUMN_1_DATA), e.getMessage());
            throw e;
        }
    }

    private TestObject createTestObject() {
        TestObject testObject = new TestObject();
        testObject.setData(TEST_COLUMN_1_DATA);
        testObject.setAnotherData(TEST_COLUMN_2_DATA);
        return testObject;
    }


    private ResultSet buildMockResultSet() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        CassandraTemplateImplTest.<Row>mockIterable(resultSet, buildMockRow());
        return resultSet;
    }


    static class TestObject {

        String data;

        String anotherData;

        public TestObject() {
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getAnotherData() {
            return anotherData;
        }

        public void setAnotherData(String anotherData) {
            this.anotherData = anotherData;
        }
    }


    public static <T> void mockIterable(Iterable<T> iterable, T... values) {
        Iterator<T> mockIterator = mock(Iterator.class);
        when(iterable.iterator()).thenReturn(mockIterator);

        if (values.length == 0) {
            when(mockIterator.hasNext()).thenReturn(false);
            return;
        } else if (values.length == 1) {
            when(mockIterator.hasNext()).thenReturn(true, false);
            when(mockIterator.next()).thenReturn(values[0]);
        } else {
            // build boolean array for hasNext()
            Boolean[] hasNextResponses = new Boolean[values.length];
            for (int i = 0; i < hasNextResponses.length - 1; i++) {
                hasNextResponses[i] = true;
            }
            hasNextResponses[hasNextResponses.length - 1] = false;
            when(mockIterator.hasNext()).thenReturn(true, hasNextResponses);
            T[] valuesMinusTheFirst = Arrays.copyOfRange(values, 1, values.length);
            when(mockIterator.next()).thenReturn(values[0], valuesMinusTheFirst);
        }
    }

    private Row buildMockRow() throws Exception {
        Row row = mock(Row.class);
        when(row.getString(0)).thenReturn(objectMapper.writeValueAsString(createTestObject()));
        return row;
    }

    public void throwNoHostAvailableException(CassandraTemplateImpl dao) {
        doThrow(NoHostAvailableException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwAuthenticationException(CassandraTemplateImpl dao) {
        doThrow(AuthenticationException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwIllegalArgumentException(CassandraTemplateImpl dao) {
        doThrow(IllegalArgumentException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwInvalidTypeException(CassandraTemplateImpl dao) {
        doThrow(InvalidTypeException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwQueryValidationException(CassandraTemplateImpl dao) {
        doThrow(AlreadyExistsException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwQueryExecutionException(CassandraTemplateImpl dao) {
        doThrow(ReadTimeoutException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwDriverException(CassandraTemplateImpl dao) {
        doThrow(DriverException.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }

    public void throwUnknownException(CassandraTemplateImpl dao) {
        doThrow(Exception.class).when(dao).bindAndExecute(any(String.class), any(ConsistencyLevel.class), Mockito.<Object>anyVararg());
    }


}