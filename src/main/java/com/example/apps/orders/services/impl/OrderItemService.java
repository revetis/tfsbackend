package com.example.apps.orders.services.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.orders.dtos.OrderItemDTO;
import com.example.apps.orders.dtos.OrderItemDTOIU;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.exceptions.OrderItemException;
import com.example.apps.orders.repositories.OrderItemRepository;
import com.example.apps.orders.services.IOrderItemService;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.exceptions.ProductVariantException;
import com.example.apps.products.entities.ProductVariantStock;
import com.example.apps.products.repositories.ProductVariantRepository;
import com.example.apps.products.entities.Product;
import com.example.apps.products.entities.SubCategory;
import com.example.apps.products.entities.MainCategory;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderItemService implements IOrderItemService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public OrderItem create(OrderItemDTOIU orderItemDTOIU, Order order) {
        ProductVariant productVariant = productVariantRepository.findById(orderItemDTOIU.getProductVariantId())
                .orElseThrow(() -> new ProductVariantException("Ürün varyantı bulunamadı."));

        ProductVariantStock stock = findStockBySize(productVariant, orderItemDTOIU.getSize());

        if (stock.getQuantity() < orderItemDTOIU.getQuantity()) {
            throw new ProductVariantException("Yetersiz stok ("
                    + (orderItemDTOIU.getSize() != null ? orderItemDTOIU.getSize() : "varsayılan") + ").");
        }

        OrderItem newOrderItem = new OrderItem();
        newOrderItem.setProductVariantId(productVariant.getId());
        newOrderItem.setProductVariantName(productVariant.getName());
        newOrderItem.setQuantity(orderItemDTOIU.getQuantity());
        newOrderItem.setPrice(productVariant.getPrice());
        newOrderItem.setSize(orderItemDTOIU.getSize());

        // Populate missing fields for DB constraints
        if (productVariant.getProduct() != null && productVariant.getProduct().getSubCategory() != null) {
            newOrderItem.setSubCategory(productVariant.getProduct().getSubCategory().getName());
            if (productVariant.getProduct().getSubCategory().getMainCategory() != null) {
                newOrderItem.setMainCategory(productVariant.getProduct().getSubCategory().getMainCategory().getName());
            } else {
                newOrderItem.setMainCategory("Genel");
            }
        } else {
            newOrderItem.setSubCategory("Genel");
            newOrderItem.setMainCategory("Genel");
        }

        // Populate details (Color, Gender)
        if (productVariant.getColor() != null) {
            newOrderItem.setColor(productVariant.getColor().getName());
        }
        if (productVariant.getProduct() != null && productVariant.getProduct().getGender() != null) {
            newOrderItem.setGender(productVariant.getProduct().getGender().name());
        }

        newOrderItem.setItemType("PHYSICAL"); // Default item type
        newOrderItem.setPaidPrice(productVariant.getPrice()); // Default to unit price for now

        newOrderItem.setOrder(order);

        return newOrderItem;
    }

    private ProductVariantStock findStockBySize(ProductVariant variant,
            com.example.apps.products.enums.ProductSize size) {
        if (variant.getStocks() == null || variant.getStocks().isEmpty()) {
            throw new ProductVariantException("Bu varyant için stok kaydı bulunamadı.");
        }

        if (size == null) {
            return variant.getStocks().get(0);
        }

        return variant.getStocks().stream()
                .filter(s -> s.getSize() != null && s.getSize() == size)
                .findFirst()
                .orElseThrow(() -> new ProductVariantException("Belirtilen beden için stok bulunamadı: " + size));
    }

    @Override
    public OrderItemDTO getById(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order Item not found"));
        OrderItemDTO response = new OrderItemDTO();
        BeanUtils.copyProperties(orderItem, response);
        return response;

    }

    @Override
    public List<OrderItemDTO> getByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            throw new OrderItemException("Order Item not found");
        }

        return orderItems.stream().map(orderItem -> {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(orderItem, orderItemDTO);

            // Fetch ProductVariant to get productId and imageUrl
            productVariantRepository.findById(orderItem.getProductVariantId())
                    .ifPresent(variant -> {
                        orderItemDTO.setProductId(variant.getProduct().getId());
                        // Get first image URL if available
                        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                            orderItemDTO.setImageUrl(variant.getImages().get(0).getUrl());
                        }
                    });

            return orderItemDTO;
        }).toList();

    }

    @Override
    public List<OrderItemDTO> getAll() {
        List<OrderItem> orderItems = orderItemRepository.findAll();
        return orderItems.stream().map(orderItem -> {
            OrderItemDTO orderItemDTO = new OrderItemDTO();
            BeanUtils.copyProperties(orderItem, orderItemDTO);
            return orderItemDTO;
        }).toList();

    }

    @Override
    @Transactional
    public void delete(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order Item not found"));
        orderItemRepository.delete(orderItem);

    }

}
