package com.commerce.product.domain;

/** Active: a product not retired. Retired: final, a retired product is never reactivated (BR-9, P-6). */
public enum ProductStatus {
    ACTIVE,
    RETIRED
}
