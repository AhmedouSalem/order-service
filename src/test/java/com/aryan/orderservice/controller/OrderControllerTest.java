package com.aryan.orderservice.controller;

import com.aryan.orderservice.dto.OrderDto;
import com.aryan.orderservice.dto.OrderRequest;
import com.aryan.orderservice.enums.OrderStatus;
import com.aryan.orderservice.model.Order;
import com.aryan.orderservice.repository.OrderRepository;
import com.aryan.orderservice.services.admin.adminOrder.AdminOrderServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private AdminOrderServiceImpl adminOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder_ReturnsCreated() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .userId(1L)
                .amount(100L)
                .totalAmount(120L)
                .discount(20L)
                .orderStatus("Placed")
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        mockMvc.perform(post("/api/microservice/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Vérification que save() a bien été appelé
        verify(orderRepository, atLeastOnce()).save(any(Order.class));
    }


    @Test
    void testGetOrderById_ReturnsOrderDto() throws Exception {
        Long id = 1L;

        Order order = mock(Order.class);
        OrderDto dto = new OrderDto();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(order.getOrderDto()).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/orders/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOrderById_ReturnsNotFound() throws Exception {
        Long id = 2L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/orders/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOrderByUserIdAndStatus_Found() throws Exception {
        Long userId = 1L;
        OrderStatus status = OrderStatus.Placed;
        OrderDto dto = new OrderDto();

        when(adminOrderService.getOrderByUserIdAndOrderStatus(userId, status)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/{userId}/{status}", userId, status))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOrderByUserIdAndStatus_NotFound() throws Exception {
        Long userId = 2L;
        OrderStatus status = OrderStatus.Delivered;

        when(adminOrderService.getOrderByUserIdAndOrderStatus(userId, status)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/{userId}/{status}", userId, status))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddOrder_UpdatesOrderSuccessfully() throws Exception {
        Long orderId = 1L;

        OrderDto orderDto = OrderDto.builder()
                .id(orderId)
                .address("123 Street")
                .orderStatus(OrderStatus.Placed)
                .totalAmount(200L)
                .discount(20L)
                .orderDescription("Updated order")
                .userId(1L)
                .amount(180L)
                .couponId(42L)
                .trackingId(UUID.randomUUID())
                .build();

        Order existingOrder = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/microservice/addorder/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk());

        verify(orderRepository).save(any(Order.class));
        verify(orderRepository).flush();
    }

    @Test
    void testGetCartByUserId_Found() throws Exception {
        Long userId = 1L;
        Order order = mock(Order.class);
        OrderDto dto = new OrderDto();

        when(orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending)).thenReturn(order);
        when(order.getOrderDto()).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/getmyplacedorder/orders/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCartByUserId_NotFound() throws Exception {
        Long userId = 2L;

        when(orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/getmyplacedorder/orders/{userId}", userId))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetMyPlacedOrders_Found() throws Exception {
        Long userId = 1L;

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        OrderDto dto1 = new OrderDto();
        OrderDto dto2 = new OrderDto();

        when(order1.getOrderDto()).thenReturn(dto1);
        when(order2.getOrderDto()).thenReturn(dto2);

        List<Order> orders = List.of(order1, order2);

        when(orderRepository.findByUserIdAndOrderStatusIn(eq(userId), anyList())).thenReturn(orders);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/myOrders/{userId}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetMyPlacedOrders_NotFound() throws Exception {
        Long userId = 2L;

        when(orderRepository.findByUserIdAndOrderStatusIn(eq(userId), anyList())).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/myOrders/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetByTracking_Found() throws Exception {
        UUID trackingId = UUID.randomUUID();

        Order order = mock(Order.class);
        OrderDto dto = new OrderDto();

        when(orderRepository.findByTrackingId(trackingId)).thenReturn(Optional.of(order));
        when(order.getOrderDto()).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/getmyplacedorder/tracking/{trackingId}", trackingId))
                .andExpect(status().isOk());
    }

    @Test
    void testGetByTracking_NotFound() throws Exception {
        UUID trackingId = UUID.randomUUID();

        when(orderRepository.findByTrackingId(trackingId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/microservice/getmyplacedorder/tracking/{trackingId}", trackingId))
                .andExpect(status().isNotFound());
    }

}
