package com.commerce.discount.application;

/** BR-8: no discount holds the identifier, or it is not a UUID (AR-3). Carries no value of the request. */
public class DiscountNotFound extends RuntimeException {

    public DiscountNotFound() {
        super("BR-8: no such discount");
    }
}
