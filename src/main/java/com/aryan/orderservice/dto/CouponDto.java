package com.aryan.orderservice.dto;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class CouponDto {
    private Long id;

    private String name;

    private String code;

    private Long discount;

    private Date expirationDate;

}