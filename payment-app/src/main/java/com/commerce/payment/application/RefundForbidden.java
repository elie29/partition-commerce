package com.commerce.payment.application;

/** AR-3: the customer of the invoice is under fraud review; nothing is recorded. The message names no customer. */
public class RefundForbidden extends RuntimeException {

    public RefundForbidden() {
        super("AR-3: a customer under fraud review can neither pay nor be refunded");
    }
}
