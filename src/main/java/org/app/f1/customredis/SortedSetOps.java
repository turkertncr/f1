package org.app.f1.customredis;

import org.app.f1.service.CustomRedisService;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class SortedSetOps<T> implements RedisCacheOps<T> {

    private final CustomRedisService redisService;
    private final Class<T> type;
    private final ToDoubleFunction<T> scoreFn;

    private final ObjectMapper mapper = new ObjectMapper();

    public SortedSetOps(CustomRedisService redisService, Class<T> type, ToDoubleFunction<T> scoreFn) {
        this.redisService = redisService;
        this.type = type;
        this.scoreFn = scoreFn;
    }

    @Override
    public void put(String realKey, Object value) {
        redisService.redisSortedSetAdd((List<T>) value, realKey, scoreFn);
    }

    @Override
    public Cache.ValueWrapper get(String realKey) {
        var elements =  redisService.redisSortedSetRangeBy(realKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        List<Object> combined = new ArrayList<>();
        for (Object o : elements) {
            if (o instanceof List<?> list) {
                combined.addAll(list);
            }
        }
        if (combined.isEmpty()) return null;
        return new SimpleValueWrapper(
                combined.stream().map(o -> mapper.readValue((String) o, type)).toList()
        );
    }

    @Override
    public void evict(String realKey) {
        redisService.redisDel(realKey);
    }
}
