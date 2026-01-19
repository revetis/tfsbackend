package com.example.apps.carts.services.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.carts.dtos.CartCheckoutDTO;
import com.example.apps.carts.dtos.CartDTO;
import com.example.apps.carts.dtos.CartItemDTO;
import com.example.apps.carts.dtos.CartItemDTOIU;
import com.example.apps.carts.entities.Cart;
import com.example.apps.carts.entities.CartItem;
import com.example.apps.carts.exceptions.CartException;
import com.example.apps.carts.repositories.CartItemRepository;
import com.example.apps.carts.repositories.CartRepository;
import com.example.apps.carts.services.ICartService;
import com.example.apps.products.entities.ProductVariant;
import com.example.apps.products.entities.ProductVariantStock;
import com.example.apps.products.exceptions.ProductVariantException;
import com.example.apps.products.repositories.ProductVariantRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final com.example.apps.campaigns.services.ICampaignService campaignService;

    @Override
    @Transactional(readOnly = true)
    public Page<CartDTO> getAllCarts(int page, int size) {
        return cartRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size))
                .map(cart -> CartDTO.builder()
                        .id(cart.getId())
                        .userId(cart.getUserId())
                        .basketNumber(cart.getBasketNumber())
                        .createdAt(cart.getCreatedAt())
                        .updatedAt(cart.getUpdatedAt())
                        .items(mapToCartItemDTOs(cart.getItems()))
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public CartPageResult getAllCarts(int start, int end, String sortField, String sortOrder, String search,
            Long userId) {
        // Build sort
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                sortOrder.equalsIgnoreCase("ASC") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC,
                sortField);

        // Get all carts and filter in memory
        List<Cart> allCarts = cartRepository.findAll(sort);

        // Apply filters
        java.util.stream.Stream<Cart> stream = allCarts.stream();

        if (userId != null) {
            stream = stream.filter(c -> c.getUserId() != null && c.getUserId().equals(userId));
        }

        if (search != null && !search.isBlank()) {
            String searchLower = search.toLowerCase();
            stream = stream.filter(
                    c -> (c.getBasketNumber() != null && c.getBasketNumber().toLowerCase().contains(searchLower)) ||
                            (c.getUserId() != null && c.getUserId().toString().contains(searchLower)));
        }

        List<Cart> filteredCarts = stream.collect(Collectors.toList());
        long totalCount = filteredCarts.size();

        // Apply pagination
        int fromIndex = Math.min(start, filteredCarts.size());
        int toIndex = Math.min(end, filteredCarts.size());
        List<Cart> pagedCarts = filteredCarts.subList(fromIndex, toIndex);

        // Map to DTOs
        List<CartDTO> dtos = pagedCarts.stream()
                .map(cart -> CartDTO.builder()
                        .id(cart.getId())
                        .userId(cart.getUserId())
                        .basketNumber(cart.getBasketNumber())
                        .createdAt(cart.getCreatedAt())
                        .updatedAt(cart.getUpdatedAt())
                        .items(mapToCartItemDTOs(cart.getItems()))
                        .build())
                .collect(Collectors.toList());

        return new CartPageResult(dtos, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public CartDTO getCartById(Long id) {
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new CartException("Cart not found"));
        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .basketNumber(cart.getBasketNumber())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(mapToCartItemDTOs(cart.getItems()))
                .build();
    }

    @Override
    @Transactional
    public void deleteCartById(Long id) {
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new CartException("Cart not found"));
        cartItemRepository.deleteAll(cart.getItems());
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    @Cacheable(value = "activeCart", key = "#userId")
    public CartDTO getCartByUserId(Long userId, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .basketNumber(generateBasketNumber())
                    .items(new java.util.ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        // Use validateCartForCheckout to get items with consistent campaign pricing
        // Pass 0.0 for shipping and null for coupon to get base campaign pricing
        List<CartItemDTO> items;
        if (!cart.getItems().isEmpty()) {
            CartCheckoutDTO checkoutData = validateCartForCheckoutInternal(cart, 0.0, null);
            items = checkoutData.getValidatedItems();
        } else {
            items = new java.util.ArrayList<>();
        }

        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .basketNumber(cart.getBasketNumber())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(items)
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cartCheckoutCache", key = "#userId"),
            @CacheEvict(value = "activeCart", key = "#userId")
    })
    public CartDTO addItemToCart(Long userId, CartItemDTOIU cartItemDTOIU, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder()
                    .userId(userId)
                    .basketNumber(generateBasketNumber())
                    .items(new java.util.ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });

        // Ensure basket number exists for legacy carts
        if (cart.getBasketNumber() == null) {
            cart.setBasketNumber(generateBasketNumber());
            cart = cartRepository.save(cart);
        }

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        ProductVariant variant = productVariantRepository.findById(cartItemDTOIU.getProductVariantId())
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));

        ProductVariantStock stock = findStockBySize(variant, cartItemDTOIU.getSize());

        // Check if item already exists
        java.util.Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getProductVariantId().equals(variant.getId())
                        && (i.getSize() == cartItemDTOIU.getSize()
                                || (i.getSize() != null && i.getSize().equals(cartItemDTOIU.getSize()))))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + cartItemDTOIU.getQuantity();

            if (stock.getQuantity() <= 0) {
                throw new CartException("Ürün stokta bulunmamaktadır.");
            }
            // If requested more than available, clamp to available
            if (newQuantity > stock.getQuantity()) {
                newQuantity = stock.getQuantity().intValue();
            }

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            if (stock.getQuantity() <= 0) {
                throw new CartException("Ürün stokta bulunmamaktadır.");
            }

            Integer quantityToAdd = cartItemDTOIU.getQuantity();
            if (stock.getQuantity() < quantityToAdd) {
                quantityToAdd = stock.getQuantity().intValue();
            }

            CartItem newCartItem = CartItem.builder()
                    .productVariantId(variant.getId())
                    .quantity(quantityToAdd)
                    .size(cartItemDTOIU.getSize())
                    .cart(cart)
                    .build();

            cartItemRepository.save(newCartItem);
            cart.getItems().add(newCartItem);
        }

        cartRepository.save(cart);
        return CartDTO.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .basketNumber(cart.getBasketNumber())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(mapToCartItemDTOs(cart.getItems()))
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cartCheckoutCache", key = "#userId"),
            @CacheEvict(value = "activeCart", key = "#userId")
    })
    public CartDTO removeItemFromCart(Long userId, Long cartItemId, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseThrow(() -> new CartException("Cart not found"));

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId()))
            throw new CartException("Cart item does not belong to the user's cart.");

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        Cart updatedCart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new CartException("Cart not found after item removal"));

        return CartDTO.builder().id(updatedCart.getId()).userId(updatedCart.getUserId())
                .basketNumber(updatedCart.getBasketNumber())
                .createdAt(updatedCart.getCreatedAt())
                .updatedAt(updatedCart.getUpdatedAt())
                .items(mapToCartItemDTOs(updatedCart.getItems()))
                .build();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cartCheckoutCache", key = "#userId"),
            @CacheEvict(value = "activeCart", key = "#userId")
    })
    public Boolean clearCart(Long userId, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseThrow(() -> new CartException("Cart not found"));

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cartCheckoutCache", key = "#userId"),
            @CacheEvict(value = "activeCart", key = "#userId")
    })
    public CartDTO updateItemQuantity(Long userId, Long cartItemId, Integer quantity, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseThrow(() -> new CartException("Cart not found"));

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId()))
            throw new CartException("Cart item does not belong to the user's cart.");

        ProductVariant variant = productVariantRepository.findById(cartItem.getProductVariantId())
                .orElseThrow(() -> new ProductVariantException("Product variant not found"));
        ProductVariantStock stock = findStockBySize(variant, cartItem.getSize());

        if (stock.getQuantity() < quantity) {
            throw new CartException("Insufficient stock. Available: " + stock.getQuantity());
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return CartDTO.builder().id(cart.getId()).userId(cart.getUserId())
                .basketNumber(cart.getBasketNumber())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .items(mapToCartItemDTOs(cart.getItems()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "cartCheckoutCache", key = "#userId")
    public CartCheckoutDTO validateCartForCheckout(Long userId, Long actualUserId, Double shippingCost,
            String couponCode) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseThrow(() -> new CartException("Cart not found"));

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        return validateCartForCheckoutInternal(cart, shippingCost, couponCode);
    }

    /**
     * Internal method for cart validation - used by both getCartByUserId and
     * validateCartForCheckout
     * to ensure consistent pricing logic.
     */
    private CartCheckoutDTO validateCartForCheckoutInternal(Cart cart, Double shippingCost, String couponCode) {
        List<ProductVariant> variants = productVariantRepository.findAllById(cart.getItems().stream()
                .map(CartItem::getProductVariantId)
                .collect(Collectors.toList()));

        Map<Long, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        // Calculate Base Subtotal (Sum of Product Price * Quantity, ignoring variant
        // discount first)
        // Actually, logic is: Compare Variant Discount Total vs Campaign Discount Total
        Map<Long, ProductVariant> finalVariantMap = variantMap; // For lambda

        // 1. Calculate Standard Variant Discount Scenario
        Double subTotalVariant = calculateCartSubtotal(cart.getItems(), variantMap);
        Double totalDiscountVariant = calculateCartTotalDiscount(cart.getItems(), variantMap);

        // 2. Check for Best Campaign
        // Prepare CampaignRequestItems
        List<com.example.apps.campaigns.dtos.CampaignRequestItem> campaignItems = new java.util.ArrayList<>();
        for (CartItem item : cart.getItems()) {
            ProductVariant v = finalVariantMap.get(item.getProductVariantId());
            if (v != null) {
                Long subCatId = (v.getProduct() != null && v.getProduct().getSubCategory() != null)
                        ? v.getProduct().getSubCategory().getId()
                        : null;
                Long mainCatId = (v.getProduct() != null && v.getProduct().getSubCategory() != null
                        && v.getProduct().getSubCategory().getMainCategory() != null)
                                ? v.getProduct().getSubCategory().getMainCategory().getId()
                                : null;

                campaignItems.add(com.example.apps.campaigns.dtos.CampaignRequestItem.builder()
                        .productId(v.getProduct().getId())
                        .variantId(v.getId())
                        .categoryId(subCatId)
                        .mainCategoryId(mainCatId)
                        .price(v.getPrice()) // Use unit price
                        .quantity(item.getQuantity())
                        .build());
            }
        }

        // Calculate potential gross total based on items map (safety check)
        Double grossTotal = campaignItems.stream()
                .mapToDouble(i -> i.getPrice().doubleValue() * i.getQuantity())
                .sum();

        // Check Winner
        com.example.apps.campaigns.dtos.CampaignDTO bestCampaign = null;
        Double campaignDiscount = 0.0;
        if (couponCode == null || couponCode.trim().isEmpty()) {
            bestCampaign = campaignService.findBestCampaign(
                    java.math.BigDecimal.valueOf(grossTotal),
                    campaignItems); // Passing items now

            if (bestCampaign != null) {
                campaignDiscount = campaignService
                        .calculateCampaignDiscount(bestCampaign, campaignItems) // Passing items now
                        .doubleValue();
            }
        }

        // Coupon logic
        com.example.apps.campaigns.dtos.CouponDTO appliedCoupon = null; // Assuming CouponDTO exists
        Double couponDiscount = 0.0;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            try {
                // Assuming validateCoupon returns CouponDTO
                com.example.apps.campaigns.dtos.CouponDTO coupon = campaignService.validateCoupon(couponCode, -1L,
                        java.math.BigDecimal.valueOf(grossTotal));
                if (coupon != null) {
                    appliedCoupon = coupon;
                    couponDiscount = campaignService
                            .calculateCouponDiscount(coupon, java.math.BigDecimal.valueOf(grossTotal)).doubleValue();
                }
            } catch (Exception e) {
                couponDiscount = 0.0;
            }
        }

        boolean useCampaign = campaignDiscount > totalDiscountVariant && campaignDiscount > couponDiscount;
        boolean useCoupon = couponDiscount > totalDiscountVariant && couponDiscount >= campaignDiscount;

        Double finalSubTotal;
        Double finalTotalDiscount;
        Double finalTax;
        Double finalAmountCalc;

        if (useCampaign) {
            finalSubTotal = calculateCartSubtotal(cart.getItems(), variantMap);
            finalTax = calculateTaxWithGlobalDiscount(cart.getItems(), variantMap, campaignDiscount, grossTotal);
            Double netInclusive = grossTotal - campaignDiscount;
            Double netExclusive = netInclusive - finalTax;
            finalTotalDiscount = finalSubTotal - netExclusive;
            finalAmountCalc = netInclusive + (shippingCost != null ? shippingCost : 0.0);

        } else if (useCoupon) {
            finalSubTotal = calculateCartSubtotal(cart.getItems(), variantMap);
            finalTax = calculateTaxWithGlobalDiscount(cart.getItems(), variantMap, couponDiscount, grossTotal);
            Double netInclusive = grossTotal - couponDiscount;
            Double netExclusive = netInclusive - finalTax;
            finalTotalDiscount = finalSubTotal - netExclusive;
            finalAmountCalc = netInclusive + (shippingCost != null ? shippingCost : 0.0);
        } else {
            // Variant
            finalSubTotal = subTotalVariant;
            finalTotalDiscount = totalDiscountVariant;
            finalTax = calculateTaxAmount(cart.getItems(), variantMap);
            finalAmountCalc = calculateFinalAmount(cart.getItems(), variantMap, shippingCost);
        }

        // Update DTOs to reflect campaign or variant discount for frontend display
        List<CartItemDTO> validatedItems = mapToCartItemDTOs(cart.getItems());
        // Subtotal is informational.
        // If we set SubTotal = Gross (Inclusive), and Discount = Campaign (Inclusive).
        // Then Sub - Disc = Net Inclusive.
        // Final = Net Inclusive + Shipping (if tax is included).
        // This seems correct for "Odenilecek Tutar".

        if (useCampaign || useCoupon) {
            Double winDisc = useCampaign ? campaignDiscount : couponDiscount;
            if (grossTotal > 0) {
                for (CartItemDTO vid : validatedItems) {
                    Double lineGross = vid.getPrice().doubleValue() * vid.getQuantity();
                    Double ratio = lineGross / grossTotal;
                    Double vidDiscTotal = winDisc * ratio;
                    Double unitVidDisc = vidDiscTotal / vid.getQuantity();
                    vid.setDiscountPrice(java.math.BigDecimal.valueOf(vid.getPrice().doubleValue() - unitVidDisc));
                }
            }
        }

        CartCheckoutDTO checkout = CartCheckoutDTO.builder().validatedItems(validatedItems)
                .subTotal(finalSubTotal)
                .totalDiscount(finalTotalDiscount)
                .shippingFee(shippingCost)
                .taxAmount(finalTax)
                .finalAmount(finalAmountCalc)
                .appliedDiscountType(useCoupon ? com.example.apps.orders.enums.AppliedDiscountType.COUPON
                        : (useCampaign ? com.example.apps.orders.enums.AppliedDiscountType.CAMPAIGN
                                : (totalDiscountVariant > 0 ? com.example.apps.orders.enums.AppliedDiscountType.VARIANT
                                        : com.example.apps.orders.enums.AppliedDiscountType.NONE)))
                .appliedDiscountName(useCoupon ? (appliedCoupon != null ? appliedCoupon.getCode() : "Kupon")
                        : (useCampaign ? (bestCampaign != null ? bestCampaign.getName() : "Kampanya") : null))
                .isStockAvailable(isStockAvailable(cart.getItems(), variantMap)).checkoutToken(generateCheckoutToken())
                .build();

        return checkout;
    }

    @Override
    @Transactional(readOnly = true)
    public CartCheckoutDTO validateGuestCart(List<CartItemDTOIU> itemsDTO, Double shippingCost, String couponCode) {
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            return CartCheckoutDTO.builder().build();
        }

        // Convert DTOs to Transient CartItems
        List<CartItem> items = itemsDTO.stream().map(dto -> {
            CartItem item = new CartItem();
            item.setProductVariantId(dto.getProductVariantId());
            item.setQuantity(dto.getQuantity());
            item.setSize(dto.getSize());
            return item;
        }).collect(Collectors.toList());

        List<ProductVariant> variants = productVariantRepository.findAllById(items.stream()
                .map(CartItem::getProductVariantId)
                .collect(Collectors.toList()));

        Map<Long, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        // Check Winner
        Double subTotalVariant = calculateCartSubtotal(items, variantMap);
        Double totalDiscountVariant = calculateCartTotalDiscount(items, variantMap);

        // Prepare CampaignRequestItems
        List<com.example.apps.campaigns.dtos.CampaignRequestItem> campaignItems = new java.util.ArrayList<>();
        for (CartItem item : items) {
            ProductVariant v = variantMap.get(item.getProductVariantId());
            if (v != null) {
                Long subCatId = (v.getProduct() != null && v.getProduct().getSubCategory() != null)
                        ? v.getProduct().getSubCategory().getId()
                        : null;
                Long mainCatId = (v.getProduct() != null && v.getProduct().getSubCategory() != null
                        && v.getProduct().getSubCategory().getMainCategory() != null)
                                ? v.getProduct().getSubCategory().getMainCategory().getId()
                                : null;

                campaignItems.add(com.example.apps.campaigns.dtos.CampaignRequestItem.builder()
                        .productId(v.getProduct().getId())
                        .variantId(v.getId())
                        .categoryId(subCatId)
                        .mainCategoryId(mainCatId)
                        .price(v.getPrice()) // Use unit price
                        .quantity(item.getQuantity())
                        .build());
            }
        }

        // Calculate potential gross total based on items map (safety check)
        Double grossTotal = campaignItems.stream()
                .mapToDouble(i -> i.getPrice().doubleValue() * i.getQuantity())
                .sum();

        // Check Winner
        com.example.apps.campaigns.dtos.CampaignDTO bestCampaign = null;
        Double campaignDiscount = 0.0;
        if (couponCode == null || couponCode.trim().isEmpty()) {
            bestCampaign = campaignService.findBestCampaign(
                    java.math.BigDecimal.valueOf(grossTotal),
                    campaignItems);
            if (bestCampaign != null) {
                campaignDiscount = campaignService
                        .calculateCampaignDiscount(bestCampaign, campaignItems)
                        .doubleValue();
            }
        }

        // Coupon logic
        com.example.apps.campaigns.dtos.CouponDTO appliedCoupon = null;
        Double couponDiscount = 0.0;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            try {
                com.example.apps.campaigns.dtos.CouponDTO coupon = campaignService.validateCoupon(couponCode, -1L,
                        java.math.BigDecimal.valueOf(grossTotal));
                if (coupon != null) {
                    appliedCoupon = coupon;
                    couponDiscount = campaignService
                            .calculateCouponDiscount(coupon, java.math.BigDecimal.valueOf(grossTotal)).doubleValue();
                }
            } catch (Exception e) {
                couponDiscount = 0.0;
            }
        }
        // Logic Choice
        boolean useCampaign = campaignDiscount > totalDiscountVariant && campaignDiscount > couponDiscount;
        boolean useCoupon = couponDiscount > totalDiscountVariant && couponDiscount >= campaignDiscount;

        Double finalSubTotal;
        Double finalTotalDiscount;
        Double finalTax;
        Double finalAmountCalc;

        if (useCampaign) {
            finalSubTotal = calculateCartSubtotal(items, variantMap);
            finalTax = calculateTaxWithGlobalDiscount(items, variantMap, campaignDiscount, grossTotal);
            Double netInclusive = grossTotal - campaignDiscount;
            Double netExclusive = netInclusive - finalTax;
            finalTotalDiscount = finalSubTotal - netExclusive;
            finalAmountCalc = netInclusive + (shippingCost != null ? shippingCost : 0.0);

        } else if (useCoupon) {
            finalSubTotal = calculateCartSubtotal(items, variantMap);
            finalTax = calculateTaxWithGlobalDiscount(items, variantMap, couponDiscount, grossTotal);
            Double netInclusive = grossTotal - couponDiscount;
            Double netExclusive = netInclusive - finalTax;
            finalTotalDiscount = finalSubTotal - netExclusive;
            finalAmountCalc = netInclusive + (shippingCost != null ? shippingCost : 0.0);
        } else {
            // Variant
            finalSubTotal = subTotalVariant;
            finalTotalDiscount = totalDiscountVariant;
            finalTax = calculateTaxAmount(items, variantMap);
            finalAmountCalc = calculateFinalAmount(items, variantMap, shippingCost);
        }

        // Update DTOs
        List<CartItemDTO> validatedItems = mapToCartItemDTOs(items);
        if (useCampaign || useCoupon) {
            Double winDisc = useCampaign ? campaignDiscount : couponDiscount;
            if (grossTotal > 0) {
                for (CartItemDTO vid : validatedItems) {
                    Double lineGross = vid.getPrice().doubleValue() * vid.getQuantity();
                    Double ratio = lineGross / grossTotal;
                    Double vidDiscTotal = winDisc * ratio;
                    Double unitVidDisc = vidDiscTotal / vid.getQuantity();
                    vid.setDiscountPrice(java.math.BigDecimal.valueOf(vid.getPrice().doubleValue() - unitVidDisc));
                }
            }
        }

        return CartCheckoutDTO.builder()
                .validatedItems(validatedItems)
                .subTotal(finalSubTotal)
                .totalDiscount(finalTotalDiscount)
                .shippingFee(shippingCost)
                .taxAmount(finalTax)
                .finalAmount(finalAmountCalc)
                .appliedDiscountType(useCoupon ? com.example.apps.orders.enums.AppliedDiscountType.COUPON
                        : (useCampaign ? com.example.apps.orders.enums.AppliedDiscountType.CAMPAIGN
                                : (totalDiscountVariant > 0 ? com.example.apps.orders.enums.AppliedDiscountType.VARIANT
                                        : com.example.apps.orders.enums.AppliedDiscountType.NONE)))
                .appliedDiscountName(useCoupon ? (appliedCoupon != null ? appliedCoupon.getCode() : "Kupon")
                        : (useCampaign ? (bestCampaign != null ? bestCampaign.getName() : "Kampanya") : null))
                .isStockAvailable(isStockAvailable(items, variantMap))
                .checkoutToken(generateCheckoutToken())
                .build();
    }

    private String generateCheckoutToken() {
        return java.util.UUID.randomUUID().toString();
    }

    private String generateBasketNumber() {
        String datePart = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randomInt = java.util.concurrent.ThreadLocalRandom.current().nextInt(100000, 999999);
        return "TFSB" + datePart + "-" + randomInt;
    }

    private Boolean isStockAvailable(List<CartItem> items, Map<Long, ProductVariant> variants) {
        ProductVariant variant = null;
        for (CartItem item : items) {

            variant = variants.get(item.getProductVariantId());
            if (variant == null) {
                throw new ProductVariantException("Product variant not found for cart item.");
            }
            ProductVariantStock stock = findStockBySize(variant, item.getSize());
            if (stock.getQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private ProductVariantStock findStockBySize(ProductVariant variant,
            com.example.apps.products.enums.ProductSize size) {
        if (variant.getStocks() == null || variant.getStocks().isEmpty()) {
            throw new ProductVariantException("No stock records found for this variant.");
        }

        if (size == null) {
            return variant.getStocks().get(0);
        }

        return variant.getStocks().stream()
                .filter(s -> s.getSize() != null && s.getSize() == size)
                .findFirst()
                .orElseThrow(() -> new ProductVariantException("Stock not found for size: " + size));
    }

    private Double calculateFinalAmount(List<CartItem> items, Map<Long, ProductVariant> variants, Double shippingCost) {
        Double subtotal = calculateCartSubtotal(items, variants);
        Double totalDiscount = calculateCartTotalDiscount(items, variants);
        Double taxAmount = calculateTaxAmount(items, variants);

        // Final = (Subtotal_Excl - Discount_Excl) + Tax + Shipping
        double finalAmount = subtotal - totalDiscount + taxAmount + (shippingCost != null ? shippingCost : 0.0);
        return finalAmount > 0 ? finalAmount : 0.0;
    }

    private Double calculateTaxAmount(List<CartItem> items, Map<Long, ProductVariant> variants) {
        Double taxAmount = 0.0;
        for (CartItem item : items) {
            ProductVariant variant = variants.get(item.getProductVariantId());
            if (variant == null)
                continue;

            // Use Discount Price if available as the base for tax calculation, or standard
            // price
            Double unitPrice = variant.getDiscountPrice() != null ? variant.getDiscountPrice().doubleValue()
                    : variant.getPrice().doubleValue();

            Double taxRatio = (variant.getProduct() != null && variant.getProduct().getTaxRatio() != null)
                    ? variant.getProduct().getTaxRatio()
                    : 12.0;

            Double lineTotalInclusive = unitPrice * item.getQuantity();

            // Extract Tax: inclusive - (inclusive / (1 + rate/100))
            Double itemTax = lineTotalInclusive - (lineTotalInclusive / (1 + taxRatio / 100.0));
            taxAmount += itemTax;
        }
        return taxAmount;
    }

    private Double calculateTaxWithGlobalDiscount(List<CartItem> items, Map<Long, ProductVariant> variants,
            Double totalDiscount, Double grossTotal) {
        Double taxAmount = 0.0;
        if (grossTotal <= 0)
            return 0.0;

        for (CartItem item : items) {
            ProductVariant variant = variants.get(item.getProductVariantId());
            if (variant == null)
                continue;

            Double lineTotalGross = variant.getPrice().doubleValue() * item.getQuantity();

            // Distribute discount
            Double ratio = lineTotalGross / grossTotal;
            Double itemDiscount = totalDiscount * ratio;
            Double lineTotalNet = lineTotalGross - itemDiscount;

            Double taxRatio = (variant.getProduct() != null && variant.getProduct().getTaxRatio() != null)
                    ? variant.getProduct().getTaxRatio()
                    : 12.0;

            // Extract Tax from Net
            Double itemTax = lineTotalNet - (lineTotalNet / (1 + taxRatio / 100.0));
            taxAmount += itemTax;
        }
        return taxAmount;
    }

    private Double calculateCartSubtotal(List<CartItem> items, Map<Long, ProductVariant> variants) {
        Double subtotal = 0.0;
        ProductVariant variant = null;
        for (CartItem item : items) {
            variant = variants.get(item.getProductVariantId());
            if (variant == null) {
                throw new ProductVariantException("Product variant not found for cart item.");
            }

            Double taxRatio = (variant.getProduct() != null && variant.getProduct().getTaxRatio() != null)
                    ? variant.getProduct().getTaxRatio()
                    : 12.0; // Updated default to 12.0

            // Subtotal is Pre-Tax Original Price
            double inclusivePrice = variant.getPrice().doubleValue() * item.getQuantity();
            double exclusivePrice = inclusivePrice / (1 + taxRatio / 100.0);

            subtotal += exclusivePrice;
        }
        return subtotal;
    }

    private Double calculateCartTotalDiscount(List<CartItem> items, Map<Long, ProductVariant> variants) {
        Double totalDiscount = 0.0;
        ProductVariant variant = null;
        if (variants.isEmpty()) {
            return 0.0;
        }

        for (CartItem item : items) {
            variant = variants.get(item.getProductVariantId());
            if (variant == null)
                continue;

            Double taxRatio = (variant.getProduct() != null && variant.getProduct().getTaxRatio() != null)
                    ? variant.getProduct().getTaxRatio()
                    : 12.0;

            if (variant.getDiscountPrice() != null
                    && variant.getDiscountPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                // Discount Amount Inclusive
                double discountInclusive = (variant.getPrice().doubleValue() - variant.getDiscountPrice().doubleValue())
                        * item.getQuantity();

                // Discount Amount Exclusive
                double discountExclusive = discountInclusive / (1 + taxRatio / 100.0);

                totalDiscount += discountExclusive;
            }
        }
        return totalDiscount;
    }

    private List<CartItemDTO> mapToCartItemDTOs(List<CartItem> items) {
        if (items.isEmpty())
            return new java.util.ArrayList<>();

        List<Long> variantIds = items.stream().map(CartItem::getProductVariantId).collect(Collectors.toList());
        List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);
        Map<Long, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        return items.stream().map(item -> {
            ProductVariant variant = variantMap.get(item.getProductVariantId());
            String imageUrl = null;
            if (variant != null && variant.getImages() != null && !variant.getImages().isEmpty()) {
                imageUrl = variant.getImages().get(0).getUrl();
            }

            Integer stockQty = 0;
            if (variant != null) {
                try {
                    stockQty = findStockBySize(variant, item.getSize()).getQuantity().intValue();
                } catch (Exception e) {
                }
            }

            String pName = (variant != null && variant.getProduct() != null) ? variant.getProduct().getName()
                    : "Unknown";

            Double taxRatio = (variant != null && variant.getProduct() != null
                    && variant.getProduct().getTaxRatio() != null)
                            ? variant.getProduct().getTaxRatio()
                            : 20.0;

            String mainCategory = (variant != null && variant.getProduct() != null
                    && variant.getProduct().getSubCategory() != null
                    && variant.getProduct().getSubCategory().getMainCategory() != null)
                            ? variant.getProduct().getSubCategory().getMainCategory().getName()
                            : "Diğer";

            String subCategory = (variant != null && variant.getProduct() != null
                    && variant.getProduct().getSubCategory() != null)
                            ? variant.getProduct().getSubCategory().getName()
                            : "Genel";

            String colorName = (variant != null && variant.getColor() != null) ? variant.getColor().getName() : null;
            String hexCode = (variant != null && variant.getColor() != null) ? variant.getColor().getHexCode() : null;

            return CartItemDTO.builder()
                    .id(item.getId())
                    .productVariantId(item.getProductVariantId())
                    .quantity(item.getQuantity())
                    .size(item.getSize())
                    .productName(pName)
                    .variantName(variant != null ? variant.getName() : null)
                    .colorName(colorName)
                    .hexCode(hexCode)
                    .price(variant != null ? variant.getPrice() : java.math.BigDecimal.ZERO)
                    .discountPrice(variant != null ? variant.getDiscountPrice() : null)
                    .imageUrl(imageUrl)
                    .stockQuantity(stockQty)
                    .taxRatio(taxRatio)
                    .mainCategory(mainCategory)
                    .subCategory(subCategory)
                    .productId(variant != null && variant.getProduct() != null ? variant.getProduct().getId() : null)
                    .categoryId(variant != null && variant.getProduct() != null
                            && variant.getProduct().getSubCategory() != null
                                    ? variant.getProduct().getSubCategory().getId()
                                    : null)
                    .build();
        }).collect(Collectors.toList());
    }

}
