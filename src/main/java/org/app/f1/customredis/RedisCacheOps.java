package org.app.f1.customredis;

import org.springframework.cache.Cache;

public interface RedisCacheOps<T> {
    void put(String realKey, Object value);
    Cache.ValueWrapper get(String realKey);
    void evict(String realKey);
}