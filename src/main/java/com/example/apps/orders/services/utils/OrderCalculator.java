package com.example.apps.orders.services.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CouponDTO;
import com.example.apps.orders.entities.Order;
import com.example.apps.orders.entities.OrderItem;
import com.example.apps.orders.enums.AppliedDiscountType;
import com.example.apps.products.entities.ProductVariant;

@Component
@lombok.RequiredArgsConstructor
public class OrderCalculator {

    private final com.example.apps.products.repositories.ProductVariantRepository productVariantRepository;

    /**
     * Calculates the order total, handling mutual exclusivity between Coupons and
     * Campaigns.
     * Campaign Application Rule: Campaigns override Variant usage, effectively
     * resetting price to Base Price before discount.
     * Coupon Application Rule: Coupons apply to the current effective price (which
     * is Variant Discounted Price by default).
     * Rule: Coupon AND Campaign cannot coexist. Coupon takes precedence (or logic
     * in Service decides).
     */
    public void calculateOrderTotals(Order order, CouponDTO coupon, CampaignDTO campaign) {
        // 1. Reset all items to initial state to ensure clean calculation
        // This is important because we might be re-calculating
        // However, in create() flow items are fresh.
        // We assume items currently have:
        // - Price = Effective Price (Variant Discounted if applicable)
        // - PaidPrice = Same as Price initially

        // We need access to Base Price (List Price) to implement "Campaign Overrides
        // Variant Discount" logic.
        // Since OrderItem doesn't store BasePrice separately in a guaranteed way (it
        // has unitPriceWithoutTax etc calculated from effective),
        // we might rely on the fact that if a variant discount exists, we should know
        // about it.
        // BUT, OrderItem is already created with "effectivePrice".

        // REFACTOR STRATEGY:
        // We assume the Caller (OrderService) passes items that might have Variant
        // Discounts already applied.
        // We need to know if we should REVERT to Base Price for Campaign application.

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        AppliedDiscountType globalDiscountType = AppliedDiscountType.NONE;

        if (coupon != null) {
            globalDiscountType = AppliedDiscountType.COUPON;
            // Apply Coupon logic (usually percentage off total or fixed amount)
            // Coupon applies on top of Variant Prices (SubtotalWithVariantDiscounts)

            BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
            BigDecimal couponDiscount = calculateCouponDiscount(coupon, subtotal);

            totalDiscount = couponDiscount;
            totalAmount = subtotal.subtract(couponDiscount);

            // Mark items
            for (OrderItem item : order.getOrderItems()) {
                // Keep Variant Discount if it was there?
                // If item.price < basePrice its VARIANT.
                // But we have a global coupon.
                // Let's mark as COUPON because that's the final major modifier?
                // Or better: If it had variant discount, it is VARIANT + COUPON.
                // For simplicity and requirement: "Coupon var, Kampanya yok".
                // Detailed item level tracking might be complex if mixed.
                // Let's set to COUPON to indicate Order level override.
                item.setAppliedDiscountType(AppliedDiscountType.COUPON);
            }

            order.setCouponCode(coupon.getCode());
            order.setCampaignId(null);
            order.setCampaignName(null);

        } else if (campaign != null) {
            globalDiscountType = AppliedDiscountType.CAMPAIGN;

            // Campaign Logic: Overrides Variant Discounts.
            // 1. Reset items to Base Price
            // We need to fetch original prices because OrderItems might have been
            // initialized with Variant Discounted prices.

            // Optimization: Fetch all needed variants in one go could be better, but for
            // now loop is acceptable for typical cart size.
            // Or better:
            List<Long> variantIds = order.getOrderItems().stream()
                    .map(OrderItem::getProductVariantId)
                    .collect(Collectors.toList());

            List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);
            java.util.Map<Long, ProductVariant> variantMap = variants.stream()
                    .collect(Collectors.toMap(ProductVariant::getId, v -> v));

            for (OrderItem item : order.getOrderItems()) {
                ProductVariant variant = variantMap.get(item.getProductVariantId());
                if (variant != null) {
                    // RESET to Original Price
                    item.setPrice(variant.getPrice());
                    // We should also look at tax logic if we want to be perfect, but keeping it
                    // simple:
                    // Assuming existing tax ratio logic handles the new price if we re-calc?
                    // OrderItemService calculated tax amounts based on 'effectivePrice'.
                    // If we change Price, we technically invalidate TaxAmount fields.
                    // For this MVP, we will rely on Total Amount.
                    // Ideally check if re-calc tax is needed. Use existing ratio.

                    // Simple re-calc of tax based on new price:
                    BigDecimal newPrice = variant.getPrice();
                    Double taxRatio = item.getTaxRatio();

                    BigDecimal divisor = BigDecimal.valueOf(1 + (taxRatio / 100.0));
                    BigDecimal unitPriceWithoutTax = newPrice.divide(divisor, 2, RoundingMode.HALF_UP);
                    BigDecimal taxAmountPerUnit = newPrice.subtract(unitPriceWithoutTax);
                    BigDecimal totalTaxAmount = taxAmountPerUnit.multiply(BigDecimal.valueOf(item.getQuantity()));

                    item.setUnitPriceWithTax(newPrice);
                    item.setUnitPriceWithoutTax(unitPriceWithoutTax);
                    item.setTaxAmount(totalTaxAmount);
                    item.setPaidPrice(newPrice);

                    item.setAppliedDiscountType(AppliedDiscountType.CAMPAIGN);
                }
            }

            BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
            BigDecimal campaignDiscount = calculateCampaignDiscount(campaign, subtotal);

            totalDiscount = campaignDiscount;
            totalAmount = subtotal.subtract(campaignDiscount);

            order.setCampaignId(campaign.getId());
            order.setCampaignName(campaign.getName());
            order.setCouponCode(null);

        } else {
            // No Coupon, No Campaign.
            // Subtotal is based on Variant Discounts (if any).
            BigDecimal subtotal = calculateSubtotal(order.getOrderItems());
            totalAmount = subtotal;
            totalDiscount = BigDecimal.ZERO; // Variant discount is baked into price, so Order level discount is 0.

            for (OrderItem item : order.getOrderItems()) {
                // Check if it has variant discount?
                // We don't know for sure here without Base Price comparison.
                // Default to VARIANT if we assume they might have it, or NONE.
                // Let's leave it as NONE or logic in Service sets it during creation.
                // Taking a safe bet: If price is defined, we keep as is.
                if (item.getAppliedDiscountType() == AppliedDiscountType.NONE) {
                    // Leave as NONE (or VARIANT if logic elsewhere set it)
                }
            }
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        order.setDiscountAmount(totalDiscount);
        order.setTotalAmount(totalAmount);
    }

    public BigDecimal calculateSubtotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCouponDiscount(CouponDTO coupon, BigDecimal amount) {
        if (coupon.getDiscountType() == com.example.apps.campaigns.entities.Coupon.DiscountType.PERCENTAGE) {
            return amount.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return coupon.getDiscountValue();
        }
    }

    private BigDecimal calculateCampaignDiscount(CampaignDTO campaign, BigDecimal amount) {
        if (campaign.getDiscountType() == null)
            return BigDecimal.ZERO;

        BigDecimal discount = BigDecimal.ZERO;
        switch (campaign.getDiscountType()) {
            case PERCENTAGE:
                discount = amount.multiply(campaign.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discount = campaign.getDiscountValue();
                break;
            case FREE_SHIPPING:
                // Handled via Shipping Cost usually, here just 0 price discount
                discount = BigDecimal.ZERO;
                break;
        }

        if (campaign.getMaxDiscountAmount() != null && discount.compareTo(campaign.getMaxDiscountAmount()) > 0) {
            discount = campaign.getMaxDiscountAmount();
        }

        if (discount.compareTo(amount) > 0) {
            discount = amount;
        }
        return discount;
    }
}
