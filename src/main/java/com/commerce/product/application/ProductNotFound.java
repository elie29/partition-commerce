package com.commerce.product.application;

/** BR-8: no product holds the identifier, or it is not a UUID (AR-3). Carries no value of the request. */
public class ProductNotFound extends RuntimeException {

    public ProductNotFound() {
        super("BR-8: no such product");
    }
}
