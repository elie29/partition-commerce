package com.commerce.payment.application;

/** The content of a pay breaks BR-22: refused before the invoice is read. */
public class PaymentContentRefused extends RuntimeException {

    public PaymentContentRefused(String reason) {
        super(reason);
    }
}
