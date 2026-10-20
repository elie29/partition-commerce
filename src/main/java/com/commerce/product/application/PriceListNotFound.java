package com.commerce.product.application;

/**
 * BR-9, BR-12: no price list holds the identifier, or an identifier of the path is not a UUID (AR-3). Carries no value
 * of the request.
 */
public class PriceListNotFound extends RuntimeException {

    public PriceListNotFound() {
        super("BR-9: no such price list");
    }
}
