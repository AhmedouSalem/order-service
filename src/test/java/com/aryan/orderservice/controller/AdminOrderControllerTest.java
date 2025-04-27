package com.aryan.orderservice.controller;

import com.aryan.orderservice.dto.OrderDto;
import com.aryan.orderservice.services.admin.adminOrder.AdminOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.aryan.orderservice.dto.AnalyticsResponse;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminOrderService adminOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllPlacedOrders_ReturnsOk() throws Exception {
        OrderDto dto1 = new OrderDto();
        OrderDto dto2 = new OrderDto();

        when(adminOrderService.getAllPlacedOrders()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/admin/placedOrders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testChangeOrderStatus_Success() throws Exception {
        Long orderId = 1L;
        String status = "Shipped";
        OrderDto dto = new OrderDto();

        when(adminOrderService.changeOrderStatus(orderId, status)).thenReturn(dto);

        mockMvc.perform(put("/api/admin/order/{orderId}/{status}", orderId, status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void testChangeOrderStatus_Failure() throws Exception {
        Long orderId = 2L;
        String status = "InvalidStatus";

        when(adminOrderService.changeOrderStatus(orderId, status)).thenReturn(null);

        mockMvc.perform(put("/api/admin/order/{orderId}/{status}", orderId, status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Something Went Wrong!!"));
    }

    @Test
    void testGetAnalytics_ReturnsOk() throws Exception {
        AnalyticsResponse analyticsResponse = new AnalyticsResponse(
                5L, 3L, 2L,
                10L, 8L,
                1000L, 900L
        );

        when(adminOrderService.calculateAnalytics()).thenReturn(analyticsResponse);

        mockMvc.perform(get("/api/admin/order/analytics"))
                .andExpect(status().isOk());
    }


}
