package com.commerce.product.application;

import org.springframework.stereotype.Service;

/** Reads a product: BR-8 of the spec of {@code product}. */
@Service
public class GetProductUseCase {

    private final Products products;

    public GetProductUseCase(Products products) {
        this.products = products;
    }

    /** @throws ProductNotFound when the identifier is not a UUID (AR-3) or names no product */
    public ProductView get(String productId) {
        return Identifiers.parse(productId).flatMap(products::find).orElseThrow(ProductNotFound::new);
    }
}
