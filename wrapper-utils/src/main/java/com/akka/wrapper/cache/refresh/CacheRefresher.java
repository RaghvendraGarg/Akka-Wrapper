package com.akka.wrapper.cache.refresh;

import com.akka.wrapper.cache.message.CacheRefreshSpec;

public interface CacheRefresher {

    void refresh(CacheRefreshSpec spec);

}
