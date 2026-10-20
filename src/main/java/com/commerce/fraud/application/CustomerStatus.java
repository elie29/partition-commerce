package com.commerce.fraud.application;

/**
 * The contract {@code fraud} shows {@code payment}, in process (`docs/specs/customer/fraud/spec.md`, Contracts with
 * neighbours; architect, 2026-10-20): is this customer blocked, yes or no.
 */
public interface CustomerStatus {

    /**
     * Yes while a review of the customer, named by the {@code customerId} billing holds, the same text, is open, not
     * lifted; no otherwise (BR-3, BR-4). A failure of the store reaches the caller as an exception.
     */
    boolean blocked(String customerId);
}
