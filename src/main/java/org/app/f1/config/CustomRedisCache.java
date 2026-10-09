package org.app.f1.config;

import lombok.extern.slf4j.Slf4j;
import org.app.f1.customredis.RedisCacheOps;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

@Slf4j
public class CustomRedisCache<T, R> implements Cache {

    private final String name;
    private final RedisCacheOps<T> ops;

    public CustomRedisCache(String name, RedisCacheOps<T> ops) {
        this.name = name;
        this.ops = ops;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public @Nullable ValueWrapper get(Object key) {
        return ops.get(name + ":" + key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <V> V get(Object key, @Nullable Class<V> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (V) value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <V> V get(Object key, Callable<V> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (V) wrapper.get();
        }
        V value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
        if (value != null) {
            put(key, value);
        }
        return value;
    }

    @Override
    public void put(Object key, @Nullable Object value) {
        ops.put(name + ":" + key, value);
    }

    @Override
    public void evict(Object key) {
        ops.evict(name + ":" + key);
    }

    @Override
    public void clear() {}
}
