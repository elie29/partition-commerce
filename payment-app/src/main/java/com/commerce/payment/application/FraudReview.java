package com.commerce.payment.application;

/**
 * The port to the customers under fraud review (BR-32); an outbound adapter implements it over {@code fraud}'s
 * {@code CustomerStatus}.
 */
public interface FraudReview {

    /** Whether the customer, an invoice's {@code customerId}, the same text, is under fraud review. */
    boolean underReview(String customerId);
}
