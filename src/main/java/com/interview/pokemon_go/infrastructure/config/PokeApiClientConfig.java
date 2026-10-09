package com.interview.pokemon_go.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(PokeApiProperties.class)
public class PokeApiClientConfig {

    /**
     * Bean name of the PokeAPI {@link RestClient}. It is injected by name, so a future client for
     * another service can never be wired into the PokeAPI integration by type.
     */
    public static final String POKEAPI_REST_CLIENT = "pokeApiRestClient";

    /**
     * Bean name of the executor for parallel PokeAPI calls. Injected by name because Spring Boot also
     * registers its own {@code applicationTaskExecutor}.
     */
    public static final String POKEAPI_EXECUTOR = "pokeApiExecutor";

    /**
     * Built by hand: the project has no RestClient auto-configuration. Timeouts are bounded so a slow
     * PokeAPI fails fast with a 503 instead of holding request threads.
     */
    @Bean(POKEAPI_REST_CLIENT)
    public RestClient pokeApiRestClient(PokeApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * PokeAPI calls spend their time waiting on the network, so one virtual thread per call is cheap.
     * Concurrency per request is bounded by {@code PageQuery.MAX_SIZE}. Spring closes it on shutdown.
     */
    @Bean(POKEAPI_EXECUTOR)
    public ExecutorService pokeApiExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
