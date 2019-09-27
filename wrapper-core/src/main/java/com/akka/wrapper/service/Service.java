package com.akka.wrapper.service;

/**
 * Created by gargr on 10/02/17.
 */
public interface Service<T> {

    void apply(T t) throws Exception;

}
