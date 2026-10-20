package com.commerce.product.application;

import com.commerce.product.domain.Product;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Retires a product: BR-9 of the spec of {@code product}. No operation exposes it in this version, and nothing is
 * checked about who calls it.
 */
@Service
public class RetireProductUseCase {

    private final Products products;

    public RetireProductUseCase(Products products) {
        this.products = products;
    }

    /**
     * A product already retired is left as it is, nothing written.
     *
     * @throws ProductNotFound when no product holds the identifier, nothing changed
     */
    public void retire(UUID productId) {
        Product product = products.find(productId).orElseThrow(ProductNotFound::new);
        if (!product.isRetired()) {
            products.changeStatus(product.retire());
        }
    }
}
