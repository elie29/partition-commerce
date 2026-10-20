package com.commerce.payment.application;

/** BR-21, BR-27: the identifier names no invoice, an identifier that is not a UUID included. */
public class InvoiceNotFound extends RuntimeException {

    public InvoiceNotFound() {
        super("BR-21, BR-27: no such invoice");
    }
}
