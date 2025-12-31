package com.example.apps.orders.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.apps.orders.dtos.OrderDTO;
import com.example.apps.orders.dtos.OrderDTOIU;
import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.dtos.OrderItemDTOIU;
import com.example.apps.orders.dtos.OrderAddressDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.enums.OrderStatus;
import com.example.apps.orders.repositories.OrderRepository;
import com.example.apps.products.services.IProductService;
import com.example.apps.payments.services.IPaymentService;
import com.example.apps.shipments.services.IShipmentService;
import com.example.apps.orders.entities.OrderItem;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private IProductService productService;

    @Mock
    private IPaymentService paymentService;

    @Mock
    private IShipmentService shipmentService;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDTOIU orderDTOIU;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setOrderNumber("TFS20231026-1234");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(new BigDecimal("200.00"));

        orderDTOIU = new OrderDTOIU();
        orderDTOIU.setUserId(1L);
        orderDTOIU.setCustomerEmail("user@example.com");
        orderDTOIU.setCustomerName("Test User");

        OrderAddressDTO addressDTO = new OrderAddressDTO();
        addressDTO.setContactName("Test User");
        addressDTO.setAddressLine("Test Address");
        addressDTO.setCity("Istanbul");

        orderDTOIU.setShippingAddress(addressDTO);
        orderDTOIU.setBillingAddress(addressDTO);

        OrderItemDTOIU itemDTOIU = new OrderItemDTOIU();
        itemDTOIU.setProductVariantId(1L);
        itemDTOIU.setQuantity(2);
        orderDTOIU.setItems(List.of(itemDTOIU));
    }

    @Test
    void createOrder_Success() {
        OrderItemDTO savedItem = new OrderItemDTO();
        savedItem.setProductVariantId(1L);
        savedItem.setQuantity(2);
        savedItem.setPrice(new BigDecimal("100.00"));

        // Create mock OrderItem entity
        OrderItem mockOrderItem = new OrderItem();
        mockOrderItem.setProductVariantId(1L);
        mockOrderItem.setQuantity(2);
        mockOrderItem.setPrice(new BigDecimal("100.00"));

        when(orderItemService.create(any(), any())).thenReturn(mockOrderItem);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.create(orderDTOIU);

        assertNotNull(result);
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());
        verify(productService).decreaseStock(anyLong(), anyLong(), any());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderItemDTO item = new OrderItemDTO();
        item.setProductVariantId(1L);
        item.setQuantity(2);
        when(orderItemService.getByOrderId(1L)).thenReturn(List.of(item));

        doThrow(new RuntimeException("No payment")).when(paymentService).returnPayment(anyString());
        orderService.cancel(1L, null);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(productService).increaseStock(anyLong(), anyLong(), any());
        verify(orderRepository).save(order);
    }
}
