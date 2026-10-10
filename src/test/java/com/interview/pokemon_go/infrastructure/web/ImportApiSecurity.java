package com.interview.pokemon_go.infrastructure.web;

import com.interview.pokemon_go.infrastructure.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Loads the real route policy and security error responses into a {@code @WebMvcTest} slice, which
 * does not pick up {@code @Configuration} classes on its own. Protected requests then need
 * {@code SecurityMockMvcRequestPostProcessors.jwt()}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({SecurityConfig.class, ApiSecurityErrorHandler.class})
public @interface ImportApiSecurity {
}
