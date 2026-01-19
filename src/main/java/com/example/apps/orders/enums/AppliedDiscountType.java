package com.example.apps.orders.enums;

public enum AppliedDiscountType {
    NONE,
    VARIANT,   // Product specific discount (from ProductVariant)
    COUPON,    // General coupon applied to the order
    CAMPAIGN   // Campaign applied (Campaign overrides VARIANT)
}
