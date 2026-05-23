package com.cinemabooking.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .httpBasic(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/ping",

                                "/api/v1/movies",
                                "/api/v1/movies/**",

                                "/api/v1/banners",
                                "/api/v1/banners/**",

                                "/api/v1/cinemas",
                                "/api/v1/cinemas/**",

                                "/api/v1/bookings",
                                "/api/v1/bookings/**",

                                "/api/v1/showtimes",
                                "/api/v1/showtimes/**",

                                "/api/v1/seats",
                                "/api/v1/seats/**",

                                "/api/v1/health",
                                "/api/v1/version"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/profile",
                                "/api/v1/profile/**",
                                "/api/v1/booking/**",
                                "/api/v1/payment/**",
                                "/api/v1/user/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        new FirebaseTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}