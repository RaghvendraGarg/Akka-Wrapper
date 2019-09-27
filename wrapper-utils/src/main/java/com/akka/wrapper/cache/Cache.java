package com.akka.wrapper.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.akka.wrapper.cache.message.CacheRefreshSpec;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Created by gargr on 24/11/16.
 */
public abstract class Cache<V> {

    private static final String KEY_SEPERATOR = "~";

    public static final String DEFAULT = "NULLOREMPTYVALUE";

    protected LoadingCache<String, V> cache;

    private String cacheName;

    public Cache(String cacheName, String... possibleKeyNames) {
        this.cacheName = cacheName;
        CacheNames.addCacheNameAndKeys(cacheName, possibleKeyNames);
        this.cache = buildCache(this::getValueForCache);
    }

    protected abstract V getValueForCache(String k) throws Exception;

    public V getValue(String... k) throws Exception {
        if (k == null) {
            return null;
        }
        return cache.get(getKey(k));
    }

    private LoadingCache<String, V> buildCache(Function_WithExceptions<String, V> loader) {
        return CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(getExpiryTime(), TimeUnit.MINUTES)
                .build(
                        new CacheLoader<String, V>() {
                            public V load(String key) throws Exception {
                                return loader.apply(key);
                            }
                        }
                );
    }

    public void invalidate(String... k) {
        cache.invalidate(getKey(k));
    }

    void invalidateForKey(String k) {
        cache.invalidate(k);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    @FunctionalInterface
    public interface Function_WithExceptions<T, R> {

        R apply(T t) throws Exception;
    }

    protected String getKey(String... args) {
        List<String> keyNames = CacheNames.getKeyNames(cacheName);
        if (keyNames.size() < args.length) {
            return StringUtils.join(args, KEY_SEPERATOR);
        }
        String[] argValues = createArgumentArrayWithDefaultValues(keyNames.size());
        for (int i = 0; i < args.length; i++) {
            if (StringUtils.isNotBlank(args[i])) {
                argValues[i] = args[i];
            }
        }
        return StringUtils.join(argValues, KEY_SEPERATOR);
    }

    private String[] createArgumentArrayWithDefaultValues(int size) {
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            array[i] = DEFAULT;
        }
        return array;
    }

    protected String[] getKeyParts(String key) {
        String[] split = StringUtils.split(key, KEY_SEPERATOR);
        if (ArrayUtils.isNotEmpty(split)) {
            for (int i = 0; i < split.length; i++) {
                if (DEFAULT.equalsIgnoreCase(split[i])) {
                    split[i] = null;
                }
            }
        }
        return split;
    }

    public void refresh(CacheRefreshSpec spec) {
        if (StringUtils.equalsIgnoreCase(cacheName, spec.getCacheName())) {
            List<String> keyNames = CacheNames.getKeyNames(cacheName);
            List<String> keyValues = new ArrayList<>();
            keyNames.stream().forEach(k -> keyValues.add(spec.getAttributeValue(k)));
            if (CollectionUtils.isNotEmpty(keyValues)) {
                invalidate(keyValues.toArray(new String[keyNames.size()]));
            }
        }
    }

    Map<String, V> getAllCachedData() {
        final ConcurrentMap<String, V> stringVConcurrentMap = cache.asMap();
        return new HashMap<>(stringVConcurrentMap);
    }

    public String getCacheName() {
        return cacheName;
    }

    protected long getExpiryTime() {
        return 10;
    }

}
