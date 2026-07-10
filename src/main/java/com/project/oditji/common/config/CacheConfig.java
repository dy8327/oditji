package com.project.oditji.common.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String TMDB_SEARCH_CACHE =
            "tmdbSearchCache";

    public static final String TMDB_PROVIDER_CACHE =
            "tmdbProviderCache";

    @Bean
    public CacheManager cacheManager() {

        CaffeineCache searchCache =
                new CaffeineCache(
                        TMDB_SEARCH_CACHE,
                        Caffeine.newBuilder()
                                .maximumSize(500)
                                .expireAfterWrite(
                                        Duration.ofMinutes(10)
                                )
                                .build()
                );

        CaffeineCache providerCache =
                new CaffeineCache(
                        TMDB_PROVIDER_CACHE,
                        Caffeine.newBuilder()
                                .maximumSize(5000)
                                .expireAfterWrite(
                                        Duration.ofHours(6)
                                )
                                .build()
                );

        SimpleCacheManager cacheManager =
                new SimpleCacheManager();

        cacheManager.setCaches(
                java.util.List.of(
                        searchCache,
                        providerCache
                )
        );

        return cacheManager;
    }
}