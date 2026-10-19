package com.commerce.invoice.application;

/** BR-15, BR-19: the identifier names no invoice, an identifier that is not a UUID included. */
public class InvoiceNotFound extends RuntimeException {

    public InvoiceNotFound() {
        super("BR-15, BR-19: no such invoice");
    }
}
