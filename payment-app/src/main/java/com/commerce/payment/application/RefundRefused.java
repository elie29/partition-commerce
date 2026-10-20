package com.commerce.payment.application;

/** BR-25, BR-29: the invoice is not paid, or the refund exceeds what remains of the amount paid: 409. */
public class RefundRefused extends RuntimeException {

    public RefundRefused(String reason) {
        super(reason);
    }
}
