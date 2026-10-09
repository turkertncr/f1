package org.app.f1.config;

import org.app.f1.customredis.SortedSetOps;
import org.app.f1.customredis.StringOps;
import org.app.f1.dto.response.*;
import org.app.f1.service.CustomRedisService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    private final CustomRedisService redisService;

    public CacheConfig(CustomRedisService redisService) {
        this.redisService = redisService.expireAfter(10, TimeUnit.MINUTES);
    }

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
                new CustomRedisCache<>("car_data", new SortedSetOps<>(redisService, CarDataResponse.class,
                        response -> response.date().toEpochMilli())),

                new CustomRedisCache<>("locations", new SortedSetOps<>(redisService, LocationResponse.class,
                        response -> response.date().toEpochMilli())),

                new CustomRedisCache<>("meeting_responses", new StringOps<>(redisService, MeetingResponse.class)),

                new CustomRedisCache<>("sessions_responses", new StringOps<>(redisService, SessionResponse.class)),

                new CustomRedisCache<>("drivers", new StringOps<>(redisService, DriverResponse.class)),

                new CustomRedisCache<>("laps", new SortedSetOps<>(redisService, LapResponse.class,
                        LapResponse::lapNumber)),

                new CustomRedisCache<>("paces", new StringOps<>(redisService, PaceResponse.class)),

                new CustomRedisCache<>("results", new StringOps<>(redisService, ResultResponse.class)),

                new CustomRedisCache<>("stints", new StringOps<>(redisService, StintResponse.class)),

                new CustomRedisCache<>("drivers_standings", new StringOps<>(redisService, DriverStandingsResponse.class)),

                new CustomRedisCache<>("teams_standings", new StringOps<>(redisService, TeamStandingsResponse.class)
        )));
        return cacheManager;
    }
}
