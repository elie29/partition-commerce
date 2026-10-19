package com.commerce.payment.application;

/**
 * The port to the customers under fraud review (AR-3); an outbound adapter implements it over the list the risk team
 * keeps, until the risk team has a system.
 */
public interface FraudReview {

    /** Whether the customer, an invoice's {@code customerId} matched exactly, is under fraud review. */
    boolean underReview(String customerId);
}
