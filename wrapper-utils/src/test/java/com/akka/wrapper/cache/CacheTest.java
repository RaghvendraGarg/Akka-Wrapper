package com.akka.wrapper.cache;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.akka.wrapper.cache.message.CacheRefreshSpec;


/**
 * Created by gargr on 25/01/17.
 */
public class CacheTest {

    public static final String KEY_1 = "KEY1";
    public static final String KEY_2 = "KEY2";
    private CacheImpl cache = new CacheImpl("testCache", KEY_1, KEY_2);

    @Spy
    private AnyMockClass anyMockClass;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        cache.setAnyMockClass(anyMockClass);
    }

    @Test
    public void getValueWhenDataIsNotInCache() throws Exception {
        when(anyMockClass.getValue(cache.getKey(KEY_1))).thenReturn(KEY_1);
        when(anyMockClass.getValue(cache.getKey(KEY_1))).thenReturn(KEY_2);
        String key1 = cache.getValue(KEY_1);
        verify(anyMockClass).getValue(cache.getKey(KEY_1));

        key1 = cache.getValue(KEY_1);
        verifyNoMoreInteractions(anyMockClass);

        key1 = cache.getValue(KEY_2);
        verify(anyMockClass).getValue(cache.getKey(KEY_1));
    }

    @Test
    public void invalidate()throws Exception {
        String key1 = cache.getValue(KEY_1);
        verify(anyMockClass).getValue(cache.getKey(KEY_1));

        key1 = cache.getValue(KEY_1);
        verifyNoMoreInteractions(anyMockClass);

        cache.invalidate(KEY_1);
        key1 = cache.getValue(KEY_1);
        verify(anyMockClass, times(2)).getValue(cache.getKey(KEY_1));
    }

    @Test
    public void invalidateAll() throws Exception{
        String key1 = cache.getValue(KEY_1);
        key1 = cache.getValue(KEY_2);

        key1 = cache.getValue(KEY_1);
        key1 = cache.getValue(KEY_2);

        cache.invalidateAll();
        key1 = cache.getValue(KEY_1);
        verify(anyMockClass, times(2)).getValue(cache.getKey(KEY_1));
        key1 = cache.getValue(KEY_2);
        verify(anyMockClass, times(2)).getValue(cache.getKey(KEY_1));
    }

    @Test
    public void refresh() throws Exception{
        cache = new CacheImpl("testCache", "source", "inventoryOwner", "stockType");
        cache.setAnyMockClass(anyMockClass);
        CacheRefreshSpec cacheRefreshSpec = new CacheRefreshSpecFactory().create("testCache", "Homenet", null, "ALL");

        String key1 = cache.getValue("Homenet", null, "ALL");
        key1 = cache.getValue("Homenet", "gmps-21", "NEW");

        cache.refresh(cacheRefreshSpec);
        key1 = cache.getValue("Homenet", null, "ALL");
        verify(anyMockClass, times(2)).getValue("Homenet~NULLOREMPTYVALUE~ALL");
    }

    @Test
    public void getKey(){
        cache = new CacheImpl("testCache", "source", "stockType", "inventoryOwner");
        cache.setAnyMockClass(anyMockClass);
        String[] keyNames = new String[]{"source", "stockType","inventoryOwner"};
        String key = cache.getKey("homenet", null, "gmps-21");
        Assert.assertEquals("homenet~NULLOREMPTYVALUE~gmps-21", key);
        key = cache.getKey(null, "ALL", "gmps-21");
        Assert.assertEquals("NULLOREMPTYVALUE~ALL~gmps-21", key);
        key = cache.getKey("homenet", "NEW", "");
        Assert.assertEquals("homenet~NEW~NULLOREMPTYVALUE", key);
    }

    @Test
    public void getKeyParts(){
        cache = new CacheImpl("testCache", "source", "stockType", "inventoryOwner");
        cache.setAnyMockClass(anyMockClass);
        Assert.assertArrayEquals(new String[]{"homenet", null, "gmps-21"},  cache.getKeyParts("homenet~NULLOREMPTYVALUE~gmps-21"));
        Assert.assertArrayEquals(new String[]{null, "ALL", "gmps-21"},  cache.getKeyParts("NULLOREMPTYVALUE~ALL~gmps-21"));
        Assert.assertArrayEquals(new String[]{"homenet", "NEW", null},  cache.getKeyParts("homenet~NEW~NULLOREMPTYVALUE"));
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