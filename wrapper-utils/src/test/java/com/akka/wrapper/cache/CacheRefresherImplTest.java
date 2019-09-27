package com.akka.wrapper.cache;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.akka.wrapper.cache.message.CacheRefreshSpec;
import com.akka.wrapper.cache.refresh.CacheRefresherImpl;


public class CacheRefresherImplTest {

    private CacheRefresherImpl cacheRefresher = new CacheRefresherImpl();

    @Mock
    private CacheRefreshSpec cacheRefreshSpec;

    @Mock
    private Cache cache1;

    @Mock
    private Cache cache2;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        List<Cache> caches = new ArrayList<>();
        caches.add(cache1);
        caches.add(cache2);
        ReflectionTestUtils.setField(cacheRefresher, "list", caches);
    }

    @Test
    public void refresh(){
        cacheRefresher.refresh(cacheRefreshSpec);
        Mockito.verify(cache1).refresh(cacheRefreshSpec);
        Mockito.verify(cache2).refresh(cacheRefreshSpec);
    }

}