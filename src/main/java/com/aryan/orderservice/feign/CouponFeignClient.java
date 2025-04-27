package com.aryan.orderservice.feign;

import com.aryan.orderservice.dto.CouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient("coupon-service")
public interface CouponFeignClient {
    @GetMapping("/api/admin/coupons/{id}")
    Optional<CouponDto> getCouponDtoByCode(@PathVariable("code") String code);
}
