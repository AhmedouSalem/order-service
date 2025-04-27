package com.aryan.orderservice.feign;

import com.aryan.orderservice.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("product-service")
public interface ProductFeign {
    @GetMapping("/product/{productId}")
    ProductDto getProductById(@PathVariable Long productId) ;
}
