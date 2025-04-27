package com.aryan.orderservice.repository;

import com.aryan.orderservice.enums.OrderStatus;
import com.aryan.orderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import java.util.UUID;

import java.util.Date;
import java.util.List;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testFindByUserIdAndOrderStatus() {
        Order order = Order.builder()
                .userId(123L)
                .amount(100L)
                .orderStatus(OrderStatus.Placed)
                .date(new Date())
                .build();

        orderRepository.save(order);

        Order found = orderRepository.findByUserIdAndOrderStatus(123L, OrderStatus.Placed);

        assertThat(found).isNotNull();
        assertThat(found.getUserId()).isEqualTo(123L);
        assertThat(found.getOrderStatus()).isEqualTo(OrderStatus.Placed);
    }

    @Test
    void testFindByUserIdAndOrderStatus_NotFound() {
        Order result = orderRepository.findByUserIdAndOrderStatus(999L, OrderStatus.Delivered);
        assertThat(result).isNull();
    }

    @Test
    void testFindByTrackingId_Found() {
        UUID trackingId = UUID.randomUUID();

        Order order = Order.builder()
                .userId(1L)
                .orderStatus(OrderStatus.Placed)
                .date(new Date())
                .trackingId(trackingId)
                .build();

        orderRepository.save(order);

        Optional<Order> result = orderRepository.findByTrackingId(trackingId);

        assertThat(result).isPresent();
        assertThat(result.get().getTrackingId()).isEqualTo(trackingId);
    }

    @Test
    void testFindByTrackingId_NotFound() {
        Optional<Order> result = orderRepository.findByTrackingId(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test
    void testFindByDateBetweenAndOrderStatus() {
        // Début et fin du mois courant
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date start = cal.getTime();

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date end = cal.getTime();

        Order order1 = Order.builder()
                .userId(1L)
                .orderStatus(OrderStatus.Delivered)
                .date(new Date()) // aujourd'hui
                .build();

        Order order2 = Order.builder()
                .userId(2L)
                .orderStatus(OrderStatus.Delivered)
                .date(new Date()) // aujourd'hui
                .build();

        Order order3 = Order.builder()
                .userId(3L)
                .orderStatus(OrderStatus.Shipped)
                .date(new Date()) // mauvais statut
                .build();

        orderRepository.saveAll(List.of(order1, order2, order3));

        List<Order> result = orderRepository.findByDateBetweenAndOrderStatus(start, end, OrderStatus.Delivered);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(o -> o.getOrderStatus() == OrderStatus.Delivered);
    }


}
