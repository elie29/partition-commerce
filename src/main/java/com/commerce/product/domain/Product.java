package com.commerce.product.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * A product: what the shop sells, known by its reference (BR-1), named by its label, created at {@code createdAt}. A
 * new product is active; {@link #retire} is the one move of its status, and nothing makes it active again (BR-9, P-6).
 */
public record Product(UUID productId, String reference, String label, Instant createdAt, ProductStatus status) {

    public static Product create(UUID productId, String reference, String label, Instant createdAt) {
        return new Product(productId, reference, label, createdAt, ProductStatus.ACTIVE);
    }

    /** The product retired; a product already retired stays as it is. */
    public Product retire() {
        return isRetired() ? this : new Product(productId, reference, label, createdAt, ProductStatus.RETIRED);
    }

    public boolean isRetired() {
        return status == ProductStatus.RETIRED;
    }

    /** The label and the reference are never written to a log (BR-28). */
    @Override
    public String toString() {
        return "Product[" + productId + "]";
    }
}
