package com.aryan.orderservice.services.admin.adminOrder;

import com.aryan.orderservice.dto.AnalyticsResponse;
import com.aryan.orderservice.dto.OrderDto;
import com.aryan.orderservice.dto.UserDto;
import com.aryan.orderservice.enums.OrderStatus;
import com.aryan.orderservice.feign.UserClient;
import com.aryan.orderservice.model.Order;
import com.aryan.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

class AdminOrderServiceImplTest {

    private OrderRepository orderRepository;
    private AdminOrderServiceImpl adminOrderService;
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        userClient = mock(UserClient.class);
        adminOrderService = new AdminOrderServiceImpl(orderRepository, userClient);
    }

    @Test
    void testGetAllPlacedOrders() {
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        OrderDto dto1 = new OrderDto();
        OrderDto dto2 = new OrderDto();

        when(order1.getOrderDto()).thenReturn(dto1);
        when(order2.getOrderDto()).thenReturn(dto2);

        List<Order> orders = List.of(order1, order2);
        when(orderRepository.findAllByOrderStatusIn(List.of(
                OrderStatus.Placed, OrderStatus.Shipped, OrderStatus.Delivered)))
                .thenReturn(orders);

        // Mock de userClient
        UserDto userDto = new UserDto();
        ResponseEntity<UserDto> responseEntity = ResponseEntity.ok(userDto);
        when(userClient.getUserById(anyLong())).thenReturn(responseEntity);

        // Exécution
        List<OrderDto> result = adminOrderService.getAllPlacedOrders();

        // Vérifications
        assertEquals(2, result.size());
        assertTrue(result.contains(dto1));
        assertTrue(result.contains(dto2));
        verify(orderRepository, times(1)).findAllByOrderStatusIn(anyList());
        verify(userClient, times(2)).getUserById(anyLong()); // chaque order
    }

    @Test
    void testChangeOrderStatusToShipped() {
        // Préparation
        Long orderId = 1L;
        String status = "Shipped";

        Order order = mock(Order.class);
        OrderDto expectedDto = new OrderDto();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        doNothing().when(order).setOrderStatus(OrderStatus.Shipped);
        when(orderRepository.save(order)).thenReturn(order);
        when(order.getOrderDto()).thenReturn(expectedDto);

        // Exécution
        OrderDto result = adminOrderService.changeOrderStatus(orderId, status);

        // Vérification
        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(order).setOrderStatus(OrderStatus.Shipped);
        verify(orderRepository).save(order);
    }

    @Test
    void testChangeOrderStatus_OrderNotFound() {
        // Préparation
        Long orderId = 2L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Exécution
        OrderDto result = adminOrderService.changeOrderStatus(orderId, "Shipped");

        // Vérification
        assertNull(result);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void testCalculateAnalytics() {
        AdminOrderServiceImpl spyService = spy(new AdminOrderServiceImpl(orderRepository, userClient));

        // Mock du comportement interne
        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);

        when(spyService.getTotalOrdersForMonths(currentDate.getMonthValue(), currentDate.getYear())).thenReturn(10L);
        when(spyService.getTotalOrdersForMonths(previousMonth.getMonthValue(), previousMonth.getYear())).thenReturn(8L);

        when(spyService.getTotalEarningsForMonth(currentDate.getMonthValue(), currentDate.getYear())).thenReturn(1000L);
        when(spyService.getTotalEarningsForMonth(previousMonth.getMonthValue(), previousMonth.getYear())).thenReturn(900L);

        when(orderRepository.countByOrderStatus(OrderStatus.Placed)).thenReturn(5L);
        when(orderRepository.countByOrderStatus(OrderStatus.Shipped)).thenReturn(3L);
        when(orderRepository.countByOrderStatus(OrderStatus.Delivered)).thenReturn(2L);

        // Exécution
        AnalyticsResponse result = spyService.calculateAnalytics();

        // Vérifications
        assertNotNull(result);
        assertEquals(5L, result.getPlaced());
        assertEquals(3L, result.getShipped());
        assertEquals(2L, result.getDelivered());
        assertEquals(10L, result.getCurrentMonthOrders());
        assertEquals(8L, result.getPreviousMonthOrders());
        assertEquals(1000L, result.getCurrentMonthEarnings());
        assertEquals(900L, result.getPreviousMonthEarnings());

        verify(orderRepository).countByOrderStatus(OrderStatus.Placed);
        verify(orderRepository).countByOrderStatus(OrderStatus.Shipped);
        verify(orderRepository).countByOrderStatus(OrderStatus.Delivered);
    }

    @Test
    void testGetOrderByUserIdAndOrderStatus_Found() {
        Long userId = 123L;
        OrderStatus status = OrderStatus.Placed;

        Order order = mock(Order.class);
        OrderDto dto = new OrderDto();

        when(orderRepository.findByUserIdAndOrderStatus(userId, status)).thenReturn(order);
        when(order.getOrderDto()).thenReturn(dto);

        OrderDto result = adminOrderService.getOrderByUserIdAndOrderStatus(userId, status);

        assertNotNull(result);
        assertEquals(dto, result);
        verify(orderRepository).findByUserIdAndOrderStatus(userId, status);
    }

    @Test
    void testGetOrderByUserIdAndOrderStatus_NotFound() {
        Long userId = 456L;
        OrderStatus status = OrderStatus.Delivered;

        when(orderRepository.findByUserIdAndOrderStatus(userId, status)).thenReturn(null);

        OrderDto result = adminOrderService.getOrderByUserIdAndOrderStatus(userId, status);

        assertNull(result);
        verify(orderRepository).findByUserIdAndOrderStatus(userId, status);
    }

    @Test
    void testGetTotalOrdersForMonths() {
        int month = 4; // Avril
        int year = 2024;

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        List<Order> orders = List.of(order1, order2);

        when(orderRepository.findByDateBetweenAndOrderStatus(any(Date.class), any(Date.class), eq(OrderStatus.Delivered)))
                .thenReturn(orders);

        Long result = adminOrderService.getTotalOrdersForMonths(month, year);

        assertEquals(2L, result);
        verify(orderRepository).findByDateBetweenAndOrderStatus(any(Date.class), any(Date.class), eq(OrderStatus.Delivered));
    }

    @Test
    void testGetTotalEarningsForMonth() {
        int month = 4;
        int year = 2024;

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        when(order1.getAmount()).thenReturn(100L);
        when(order2.getAmount()).thenReturn(200L);

        List<Order> orders = List.of(order1, order2);

        when(orderRepository.findByDateBetweenAndOrderStatus(any(Date.class), any(Date.class), eq(OrderStatus.Delivered)))
                .thenReturn(orders);

        Long result = adminOrderService.getTotalEarningsForMonth(month, year);

        assertEquals(300L, result);
        verify(orderRepository).findByDateBetweenAndOrderStatus(any(Date.class), any(Date.class), eq(OrderStatus.Delivered));
    }


}
