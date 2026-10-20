package com.commerce.product.application;

/**
 * BR-23: no such price list, no price for the product in it, or no price that applies, or an identifier that is not a
 * UUID (BR-29): one 404. Carries no value of the request.
 */
public class PriceNotFound extends RuntimeException {

    public PriceNotFound() {
        super("BR-23: no such product, or no price for it");
    }
}
