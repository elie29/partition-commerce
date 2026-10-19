package com.commerce.payment.application;

/** BR-21: the identifier names no invoice, an identifier that is not a UUID included. */
public class InvoiceNotFound extends RuntimeException {

    public InvoiceNotFound() {
        super("BR-21: no such invoice");
    }
}
