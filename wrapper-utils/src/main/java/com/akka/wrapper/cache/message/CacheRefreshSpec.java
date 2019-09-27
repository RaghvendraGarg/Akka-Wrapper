package com.akka.wrapper.cache.message;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class CacheRefreshSpec {

    private String cacheName;

    private final Map<String, String> attributes = new HashMap<>();

    public CacheRefreshSpec(String cacheName) {
        this.cacheName = cacheName;
    }

    private CacheRefreshSpec() {
    }

    @JsonAnySetter
    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getAttributeValue(String key) {
        return attributes.get(key);
    }

    @JsonAnyGetter
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
