package com.aryan.orderservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${ecom.token}")
    private String ecomToken;

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + ecomToken);
    }
}
