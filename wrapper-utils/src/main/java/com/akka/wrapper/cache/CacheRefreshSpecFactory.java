package com.akka.wrapper.cache;

import com.akka.wrapper.cache.message.CacheRefreshSpec;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

@Component
public class CacheRefreshSpecFactory {

    /**
     * attributeValues should be in same order as possibleKeyNames in @{@link Cache} constructor for e.g.
     * possibleKeyNames is (source, inventoryOwner) then attributeValues should be (Homenet, gmps-21) where Homenet is a source name and gmps-21 is an inventoryOwner
     *
     * @param cacheName
     * @param attributeValues
     * @return CacheRefreshSpec
     */
    public CacheRefreshSpec create(String cacheName, String... attributeValues) {
        List<String> keyNames = CacheNames.getKeyNames(cacheName);
        if (CollectionUtils.isNotEmpty(keyNames) && keyNames.size() >= attributeValues.length) {
            CacheRefreshSpec spec = new CacheRefreshSpec(cacheName);
            for (int i = 0; i < attributeValues.length; i++) {
                spec.addAttribute(keyNames.get(i), attributeValues[i]);
            }
            return spec;
        }
        return null;
    }

}