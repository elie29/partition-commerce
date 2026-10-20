package com.commerce.product.application;

import com.commerce.product.domain.Product;
import java.time.Instant;
import java.util.UUID;

/** A product as the operations answer it: its identifier, its reference, its label, its creation time (BR-5). */
public record ProductView(UUID productId, String reference, String label, Instant createdAt) {

    public static ProductView of(Product product) {
        return new ProductView(product.productId(), product.reference(), product.label(), product.createdAt());
    }

    /** The label and the reference are never written to a log (AR-2). */
    @Override
    public String toString() {
        return "ProductView[" + productId + "]";
    }
}
