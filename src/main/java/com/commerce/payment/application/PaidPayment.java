package com.commerce.payment.application;

/**
 * The payment of a paid invoice, as {@link PaymentRefunds} shows it: its charge reference and the amount charged, in
 * minor units with its currency (BR-24). {@link #toString()} shows neither (BR-31 of {@code refund}).
 */
public record PaidPayment(String chargeReference, long amountMinor, String currency) {

    @Override
    public String toString() {
        return "PaidPayment[hidden]";
    }
}
