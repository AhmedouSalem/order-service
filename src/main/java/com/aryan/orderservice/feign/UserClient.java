package com.aryan.orderservice.feign;

import com.aryan.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/{userID}")
    ResponseEntity<UserDto> getUserById(@PathVariable Long userID);
}
