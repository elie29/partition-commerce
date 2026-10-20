package com.commerce.fraud.application;

/** The store already holds an open review for the customer (BR-6); the message names no {@code customerId} (BR-9). */
public class OpenReviewHeld extends RuntimeException {

    public OpenReviewHeld() {
        super("BR-6: the customer already has an open review");
    }
}
