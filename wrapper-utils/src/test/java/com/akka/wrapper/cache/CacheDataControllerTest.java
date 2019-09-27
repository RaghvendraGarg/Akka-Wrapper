package com.akka.wrapper.cache;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;


public class CacheDataControllerTest {

    @InjectMocks
    private CacheDataController controller;

    private List<Cache> list;

    private CacheImpl cache = new CacheImpl("Cache", "name");

    protected MockMvc mockMvc;

    @Spy
    private AnyMockClass anyMockClass;

    @Before
    public void setupMockMvc() {
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(controller).build();
        list = new ArrayList<>();
        list.add(cache);
        ReflectionTestUtils.setField(controller, "list", list);
        cache.setAnyMockClass(anyMockClass);
    }

    @Test
    public void getCacheDataWhenNoCachesAreConfigured() throws Exception {
        ReflectionTestUtils.setField(controller, "list", null);
        mockMvc.perform(get("/cache/data/{cacheName}", "Test"))
                .andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    public void getCacheDataWhenCacheNameDoesNotExist() throws Exception {
        mockMvc.perform(get("/cache/data/{cacheName}", "Test"))
                .andExpect(status().isOk()).andExpect(content().string(""));
    }

    @Test
    public void getCachedData() throws Exception{
        cache.getValue("key1");
        cache.getValue("key2");
        mockMvc.perform(get("/cache/data/{cacheName}", "Cache"))
                .andExpect(status().isOk()).andExpect(content().string("{\"key1\":\"key1\",\"key2\":\"key2\"}"));
    }

    @Test
    public void getCachedDataWithCacheKey() throws Exception{
        cache.getValue("key1");
        cache.getValue("key2");
        mockMvc.perform(get("/cache/data/{cacheName}/{cacheKey}", "Cache", "key2"))
                .andExpect(status().isOk()).andExpect(content().string("{\"key2\":\"key2\"}"));
    }

    @Test
    public void getCachedDataWhenExceptionIsThrown() throws Exception{
        cache = Mockito.mock(CacheImpl.class);
        list.clear();
        list.add(cache);
        Mockito.when(cache.getCacheName()).thenReturn("Cache");
        Mockito.when(cache.getAllCachedData()).thenThrow(new RuntimeException("test ex"));
        mockMvc.perform(get("/cache/data/{cacheName}/{cacheKey}", "Cache", "key2"))
                .andExpect(status().isOk()).andExpect(content().string("{\"Error\":\"test ex\"}"));
    }


    class CacheImpl extends Cache<String> {

        public CacheImpl(String cacheName, String... possibleKeyNames) {
            super(cacheName, possibleKeyNames);
        }

        private AnyMockClass anyMockClass;

        @Override
        protected String getValueForCache(String s) {
            return anyMockClass.getValue(s);
        }

        public void setAnyMockClass(AnyMockClass anyMockClass) {
            this.anyMockClass = anyMockClass;
        }

        public String getKey(String... args) {
            return super.getKey(args);
        }

        public String[] getKeyParts(String key) {
            return super.getKeyParts(key);
        }
    }

    class AnyMockClass {

        public String getValue(String s) {
            return s ;
        }

    }


}