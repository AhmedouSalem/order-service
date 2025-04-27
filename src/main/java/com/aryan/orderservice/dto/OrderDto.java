package com.aryan.orderservice.dto;

import com.aryan.orderservice.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class OrderDto {
	private Long id;

	private String orderDescription;

	private Date date;

	private Long amount;

	private String address;

	private String payment;

	private OrderStatus orderStatus;

	private Long totalAmount;

	private Long discount;

	private UUID trackingId;

	private String userName;

	private Long userId;

	private List<CartItemsDto> cartItems;
	
	private String couponName;

	private Long couponId;

	private String couponCode;
}
