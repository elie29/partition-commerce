package com.commerce.invoice.application;

/** A payment is recorded only on an invoice still {@code ISSUED}: it is paid or cancelled, a simultaneous pay or cancel included. */
public class InvoiceNotIssued extends RuntimeException {

    public InvoiceNotIssued() {
        super("a payment is recorded only on an invoice still ISSUED");
    }
}
