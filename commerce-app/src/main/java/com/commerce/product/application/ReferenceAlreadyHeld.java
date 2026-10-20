package com.commerce.product.application;

/** BR-1: another product holds the reference. Carries no value of the request. */
public class ReferenceAlreadyHeld extends RuntimeException {

    public ReferenceAlreadyHeld() {
        super("BR-1: a product already holds this reference");
    }
}
