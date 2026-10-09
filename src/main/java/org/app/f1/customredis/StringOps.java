package org.app.f1.customredis;

import org.app.f1.service.CustomRedisService;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

public class StringOps<T> implements RedisCacheOps<T> {

    private final CustomRedisService redisService;
    private final Class<T> type;

    private final ObjectMapper mapper = new ObjectMapper();

    public StringOps(CustomRedisService redisService, Class<T> type) {
        this.redisService = redisService;
        this.type = type;
    }

    @Override
    public void put(String realKey, Object value) {
        redisService.redisStringSet(realKey, value);
    }

    @Override
    public Cache.ValueWrapper get(String realKey) {
        var replies = redisService.redisStringGet(realKey);
        if (replies == null) return null;

        String json = null;
        for (Object reply : replies) {
            if (reply instanceof String s) {
                json = s;
                break;
            }
        }
        if (json == null) return null;

        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        return new SimpleValueWrapper(mapper.readValue(json, listType));
    }

    @Override
    public void evict(String realKey) {
        redisService.redisDel(realKey);
    }
}
