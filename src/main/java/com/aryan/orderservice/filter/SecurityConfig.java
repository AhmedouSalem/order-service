package com.aryan.orderservice.filter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    @Value("${ecom.token}")
    private String ecomToken;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/admin/placedOrders").authenticated()
                .requestMatchers("/api/admin/order/**").authenticated()
                .requestMatchers("/api/microservice/**").authenticated()
                .anyRequest().denyAll()
        );


        // Ajout d’un filtre custom
        http.addFilterBefore((servletRequest, servletResponse, filterChain) -> {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String authHeader = request.getHeader("Authorization");
            String userHeader = request.getHeader("X-User");

            System.out.println("==> Incoming request to CategoryService");
            System.out.println("Authorization: " + authHeader);
            System.out.println("X-User: " + userHeader);


            if (authHeader != null && authHeader.equals("Bearer " + ecomToken)) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("system", null, List.of())
                );
            } else if (userHeader != null) {
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userHeader, null, List.of())
                );
            }

            filterChain.doFilter(servletRequest, servletResponse);
        }, UsernamePasswordAuthenticationFilter.class);

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}

