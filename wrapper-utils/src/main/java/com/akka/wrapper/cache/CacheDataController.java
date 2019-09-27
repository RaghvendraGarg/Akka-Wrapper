package com.akka.wrapper.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CacheDataController {

    @Autowired(required = false)
    private List<Cache> list;

    @RequestMapping(method = RequestMethod.GET, value = { "/cache/data/{cacheName}", "/cache/data/{cacheName}/{cacheKey}" })
    @ResponseBody
    public Map getCachedValues(@PathVariable String cacheName, @PathVariable(required = false) String cacheKey) throws Exception {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            Optional<Cache> first = list.stream().filter(c -> cacheName.equalsIgnoreCase(c.getCacheName())).findFirst();
            if (!first.isPresent()) {
                return null;
            }
            Cache cache = first.get();
            Map allCachedData = cache.getAllCachedData();
            if (StringUtils.isBlank(cacheKey)) {
                return allCachedData;
            }
            Map hashMap = new HashMap();
            hashMap.put(cacheKey, allCachedData != null ? allCachedData.get(cacheKey) : null);
            return hashMap;
        } catch (Exception e) {
            Map hashMap = new HashMap();
            hashMap.put("Error", e.getMessage());
            return hashMap;
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = { "/cache/data/{cacheName}", "/cache/data/{cacheName}/{cacheKey}" })
    @ResponseBody
    public String invalidateCachedValue(@PathVariable String cacheName, @PathVariable(required = false) String cacheKey) throws Exception {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return null;
            }
            Optional<Cache> first = list.stream().filter(c -> c.getCacheName().equalsIgnoreCase(cacheName)).findFirst();
            if (!first.isPresent()) {
                return null;
            }
            Cache cache = first.get();
            if (StringUtils.isNotBlank(cacheKey)) {
                cache.invalidateForKey(cacheKey);
                return "invalidated for key " + cacheKey;
            }
            cache.invalidateAll();
            return "invalidated " + cacheName;
        } catch (Exception e) {
            return "invalidate call failed " + e.getMessage();
        }
    }

    @RequestMapping(value = { "/cache" })
    @ResponseBody
    public Map<String, List<String>> getCacheNames() {
        return CacheNames.getCacheNames();
    }

}
