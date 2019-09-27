package com.akka.wrapper.cassandra.dao;

import com.akka.wrapper.exception.DataStorageException;

/**
 * Created by gargr on 31/01/17.
 */
public interface CassandraKeySpaceCreator {

    void create() throws DataStorageException;

}
