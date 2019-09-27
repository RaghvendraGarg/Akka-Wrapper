package com.akka.wrapper.cache.refresh;

import com.akka.wrapper.cache.Cache;
import com.akka.wrapper.cache.message.CacheRefreshSpec;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;


public class CacheRefresherImpl implements CacheRefresher {

    @Autowired(required = false)
    private List<Cache> list;

    @Override
    public void refresh(CacheRefreshSpec spec) {
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(l -> l.refresh(spec));
        }
    }
}
