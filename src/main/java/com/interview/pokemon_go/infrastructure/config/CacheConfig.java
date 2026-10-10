package com.interview.pokemon_go.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POKEAPI_POKEMON_PAGES = "pokeapi-pokemon-pages";
    public static final String POKEAPI_POKEMON = "pokeapi-pokemon";
    public static final String POKEAPI_SPECIES = "pokeapi-species";
    public static final String POKEAPI_EVOLUTION_CHAINS = "pokeapi-evolution-chains";

    /**
     * PokeAPI data is effectively static, so responses are reused for {@code pokeapi.cache.ttl};
     * the TTL still lets new Pokemon and corrections appear eventually. {@code max-entries} bounds
     * each cache's memory. The cache names are fixed, so a typo in a {@code @Cacheable} fails instead
     * of silently creating a new cache. Exceptions are never cached by Spring, so a PokeAPI outage
     * is retried on the next request.
     */
    @Bean
    public CacheManager cacheManager(PokeApiProperties properties) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(properties.cache().ttl())
                .maximumSize(properties.cache().maxEntries())
                .recordStats());
        cacheManager.setCacheNames(List.of(
                POKEAPI_POKEMON_PAGES, POKEAPI_POKEMON, POKEAPI_SPECIES, POKEAPI_EVOLUTION_CHAINS));
        return cacheManager;
    }
}
