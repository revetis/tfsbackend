package com.example.apps.carts.services.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Override
    public CartDTO getCartByUserId(Long userId, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().userId(userId).items(new java.util.ArrayList<>()).build();
            return cartRepository.save(newCart);
        });

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        return CartDTO.builder().id(cart.getId()).userId(cart.getUserId())
                .items(mapToCartItemDTOs(cart.getItems()))
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", key = "#userId")
    public CartDTO addItemToCart(Long userId, CartItemDTOIU cartItemDTOIU, Long actualUserId) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().userId(userId).items(new java.util.ArrayList<>()).build();
            return cartRepository.save(newCart);
        });
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
                // We could inform user, but for now silent capping is better than error
            }

            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            if (stock.getQuantity() <= 0) {
                throw new CartException("Ürün stokta bulunmamaktadır.");
            }

            Integer quantityToAdd = cartItemDTOIU.getQuantity();
            // If requested more than available, clamp to available
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
        return CartDTO.builder().id(cart.getId()).userId(cart.getUserId())
                .items(mapToCartItemDTOs(cart.getItems()))
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", key = "#userId")
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
                .items(mapToCartItemDTOs(updatedCart.getItems()))
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "cartCheckoutCache", key = "#userId")
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
    @CacheEvict(value = "cartCheckoutCache", key = "#userId")
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
                .items(mapToCartItemDTOs(cart.getItems()))
                .build();
    }

    @Override
    @Cacheable(value = "cartCheckoutCache", key = "#userId")
    public CartCheckoutDTO validateCartForCheckout(Long userId, Long actualUserId, Double shippingCost) {
        Cart cart = cartRepository.findCartByUserId(userId).orElseThrow(() -> new CartException("Cart not found"));

        if (!cart.getUserId().equals(actualUserId))
            throw new AccessDeniedException("You are not authorized to access this cart.");

        List<ProductVariant> variants = productVariantRepository.findAllById(cart.getItems().stream()
                .map(CartItem::getProductVariantId)
                .collect(Collectors.toList()));

        Map<Long, ProductVariant> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariant::getId, v -> v));

        CartCheckoutDTO checkout = CartCheckoutDTO.builder().validatedItems(mapToCartItemDTOs(cart.getItems()))
                .subTotal(calculateCartSubtotal(cart, variantMap))
                .totalDiscount(calculateCartTotalDiscount(cart, variantMap)).shippingFee(shippingCost)
                .taxAmount(calculateTaxAmount(cart, variantMap)).finalAmount(calculateFinalAmount(cart, variantMap))
                .isStockAvailable(isStockAvailable(cart, variantMap)).checkoutToken(generateCheckoutToken())
                .build();

        return checkout;
    }

    private String generateCheckoutToken() {
        return java.util.UUID.randomUUID().toString();
    }

    private Boolean isStockAvailable(Cart cart, Map<Long, ProductVariant> variants) {
        ProductVariant variant = null;
        for (CartItem item : cart.getItems()) {

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

    private Double calculateFinalAmount(Cart cart, Map<Long, ProductVariant> variants) {
        Double subtotal = calculateCartSubtotal(cart, variants);
        Double totalDiscount = calculateCartTotalDiscount(cart, variants);
        Double taxAmount = calculateTaxAmount(cart, variants);
        Double shippingCost = 90.0;

        return subtotal - totalDiscount + taxAmount + shippingCost;
    }

    private Double calculateTaxAmount(Cart cart, Map<Long, ProductVariant> variants) {
        Double taxAmount = 0.0;
        ProductVariant variant = null;
        for (CartItem item : cart.getItems()) {

            variant = variants.get(item.getProductVariantId());
            if (variant == null) {
                throw new ProductVariantException("Product variant not found for cart item.");
            }

            Double itemPrice = variant.getDiscountPrice() != null ? variant.getDiscountPrice().doubleValue()
                    : variant.getPrice().doubleValue();
            Double taxRatio = variant.getProduct().getTaxRatio();
            taxAmount += (itemPrice * item.getQuantity()) * taxRatio / 100;
        }
        return taxAmount;
    }

    private Double calculateCartSubtotal(Cart cart, Map<Long, ProductVariant> variants) {
        Double subtotal = 0.0;
        ProductVariant variant = null;
        for (CartItem item : cart.getItems()) {
            variant = variants.get(item.getProductVariantId());
            if (variant == null) {
                throw new ProductVariantException("Product variant not found for cart item.");
            }
            subtotal += variant.getPrice().doubleValue() * item.getQuantity();
        }
        return subtotal;
    }

    private Double calculateCartTotalDiscount(Cart cart, Map<Long, ProductVariant> variants) {
        Double totalDiscount = 0.0;
        ProductVariant variant = null;
        if (variants.isEmpty()) {
            throw new ProductVariantException("No product variants found for cart items.");
        }
        for (CartItem item : cart.getItems()) {
            variant = variants.get(item.getProductVariantId());
            if (variant == null) {
                throw new ProductVariantException("Product variant not found for cart item.");
            }
            if (variant.getDiscountPrice() != null) {
                Double discountPerItem = variant.getPrice().doubleValue() - variant.getDiscountPrice().doubleValue();
                totalDiscount += discountPerItem * item.getQuantity();
            }
        }
        return totalDiscount;
    }

    @Override
    public Page<CartDTO> getAllCarts(int page, int size) {
        Page<Cart> cartsPage = cartRepository.findAll(org.springframework.data.domain.PageRequest.of(page, size));
        return cartsPage.map(cart -> CartDTO.builder().id(cart.getId()).userId(cart.getUserId())
                .items(mapToCartItemDTOs(cart.getItems()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build());
    }

    @Override
    public CartDTO getCartById(Long id) {
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new CartException("Cart not found"));
        return CartDTO.builder().id(cart.getId()).userId(cart.getUserId())
                .items(mapToCartItemDTOs(cart.getItems()))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
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

            return CartItemDTO.builder()
                    .id(item.getId())
                    .productVariantId(item.getProductVariantId())
                    .quantity(item.getQuantity())
                    .size(item.getSize())
                    .productName(pName)
                    .variantName(variant != null ? variant.getName() : null)
                    .price(variant != null ? variant.getPrice() : java.math.BigDecimal.ZERO)
                    .imageUrl(imageUrl)
                    .stockQuantity(stockQty)
                    .build();
        }).collect(Collectors.toList());
    }

}
