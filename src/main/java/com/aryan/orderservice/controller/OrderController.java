package com.aryan.orderservice.controller;

import com.aryan.orderservice.dto.OrderDto;
import com.aryan.orderservice.dto.OrderRequest;
import com.aryan.orderservice.enums.OrderStatus;
import com.aryan.orderservice.model.Order;
import com.aryan.orderservice.repository.OrderRepository;
import com.aryan.orderservice.services.admin.adminOrder.AdminOrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/** This controller is an API for communicate with services ***/
@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AdminOrderServiceImpl adminOrderService;

    @PostMapping("/api/microservice/orders")
    public ResponseEntity<Void> createOrder(@RequestBody OrderRequest orderRequest) {
        Order order = Order.builder()
                .userId(orderRequest.getUserId())
                .amount(orderRequest.getAmount())
                .totalAmount(orderRequest.getTotalAmount())
                .discount(orderRequest.getDiscount())
                .orderStatus(OrderStatus.valueOf(orderRequest.getOrderStatus()))
                .build();

        orderRepository.save(order);


        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    @GetMapping("/api/microservice/orders/{id}")
    public ResponseEntity<Order> findOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderRepository.findById(id).orElseThrow());
    }

    @GetMapping("/api/microservice/orders/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        if (orderRepository.findById(id).isPresent()) {
            Order order = orderRepository.findById(id).get();

            return ResponseEntity.ok(orderRepository.findById(id).orElseThrow().getOrderDto());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/api/microservice/{userId}/{status}")
    public ResponseEntity<OrderDto> getOrderByUserIdAndStatus(@PathVariable Long userId, @PathVariable OrderStatus status) {
        OrderDto orderDto = adminOrderService.getOrderByUserIdAndOrderStatus(userId, status);
        if (orderDto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(orderDto);
    }

    @PutMapping("/api/microservice/addorder/orders")
    public ResponseEntity<?> addOrder(@RequestBody OrderDto orderDto) {
        Order order = this.findOrderById(orderDto.getId()).getBody();
        order.setAddress(orderDto.getAddress());
        order.setOrderStatus(orderDto.getOrderStatus());
        order.setTotalAmount(orderDto.getTotalAmount());
        order.setDiscount(orderDto.getDiscount());
        order.setOrderDescription(orderDto.getOrderDescription());
        order.setUserId(orderDto.getUserId());
        order.setAmount(orderDto.getAmount());
        order.setCouponId(orderDto.getCouponId());
        order.setCartItems(orderDto.getCartItems());
        order.setTrackingId(orderDto.getTrackingId());
        order.setDate(orderDto.getDate() != null ? order.getDate() : new Date());
        log.info("Order date is {}", order.getDate());
        orderRepository.save(order);
        orderRepository.flush();
        log.info("Order saved with new date: {}", order.getDate());

        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/microservice/getmyplacedorder/orders/{userId}")
    public ResponseEntity<OrderDto>  getCartByUserId(@PathVariable("userId") Long userId) {
        Order order =  orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        log.info(order.toString());
        return ResponseEntity.ok(order.getOrderDto());
    }

    @GetMapping("/api/microservice/myOrders/{userId}")
    public ResponseEntity<List<OrderDto>> getMyPlacedOrders(@PathVariable Long userId){
        log.info("Received request to get placed orders for user with ID: {}", userId);
        List<OrderDto> orderDtoList = orderRepository
                .findByUserIdAndOrderStatusIn(userId,
                        List.of(OrderStatus.Shipped, OrderStatus.Placed, OrderStatus.Delivered))
                .stream().map(Order::getOrderDto).collect(Collectors.toList());
        if (orderDtoList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderDtoList);
    }

    @GetMapping("/api/microservice/getmyplacedorder/tracking/{trackingId}")
    public ResponseEntity<OrderDto>  getByTracking(@PathVariable("trackingId") UUID trackingId) {
        Optional<Order> order = orderRepository.findByTrackingId(trackingId);
        if (order.isPresent()) {
            return ResponseEntity.ok(order.get().getOrderDto());
        }
        return ResponseEntity.notFound().build();
    }
}
