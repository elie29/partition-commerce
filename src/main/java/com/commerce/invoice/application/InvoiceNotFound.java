package com.commerce.invoice.application;

/** BR-15: the identifier names no invoice, an identifier that is not a UUID included. */
public class InvoiceNotFound extends RuntimeException {

    public InvoiceNotFound() {
        super("BR-15: no such invoice");
    }
}
