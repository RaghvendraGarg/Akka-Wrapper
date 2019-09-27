package com.akka.wrapper.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

public class CacheNames {

    private static final Map<String, List<String>> cacheNames = new HashMap<>();

    static void addCacheNameAndKeys(String cacheName, String... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            cacheNames.put(cacheName, new ArrayList<>());
            return;
        }
        cacheNames.put(cacheName, Arrays.asList(keys));
    }

    static List<String> getKeyNames(String cacheName) {
        return cacheNames.get(cacheName);
    }

    static Map<String, List<String>> getCacheNames() {
        return cacheNames;
    }
}
