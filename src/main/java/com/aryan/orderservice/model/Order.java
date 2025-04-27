package com.aryan.orderservice.model;

import com.aryan.orderservice.dto.CartItemsDto;
import com.aryan.orderservice.dto.CouponDto;
import com.aryan.orderservice.dto.OrderDto;
import com.aryan.orderservice.dto.UserDto;
import com.aryan.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderDescription;

    @Column
    private Date date;

    private Long amount;

    private String address;

    private String payment;

    private Long totalAmount;

    private Long discount;

    private OrderStatus orderStatus;

//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user; // Assure-toi que l'entité `User` existe

    private Long userId;

    @Transient
    private UserDto user;

//    @OneToOne(cascade = CascadeType.MERGE)
//    @JoinColumn(name = "coupon_id", referencedColumnName = "id")
    private Long couponId;

    @Transient
    private CouponDto coupon;

//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order")
//    private List<CartItems> cartItems;

    @Transient
    private List<CartItemsDto> cartItems;

    private UUID trackingId;

    public OrderDto getOrderDto() {

        return OrderDto.builder()
                .id(id)
                .orderDescription(orderDescription)
                .date(date)
                .amount(amount)
                .address(address)
                .totalAmount(totalAmount)
                .discount(discount)
                .payment(payment)
                .orderStatus(orderStatus)
                .trackingId(trackingId)
                .userId(userId)
                .userName(user != null ? user.getName() : null)
                .couponName(coupon != null ? coupon.getName() : null)
                .couponId(couponId)
                .couponCode(coupon != null ? coupon.getCode() : null)
                .cartItems(cartItems != null ? cartItems : null)
                .build(); /** À adapter pour épondre à la cart ***/

    }

}
