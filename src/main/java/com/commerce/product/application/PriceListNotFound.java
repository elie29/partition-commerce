package com.commerce.product.application;

/**
 * BR-18, BR-21: no price list holds the identifier, or an identifier of the path is not a UUID (BR-29). Carries no value
 * of the request.
 */
public class PriceListNotFound extends RuntimeException {

    public PriceListNotFound() {
        super("BR-18: no such price list");
    }
}
