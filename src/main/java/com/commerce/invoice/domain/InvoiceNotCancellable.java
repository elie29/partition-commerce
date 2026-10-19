package com.commerce.invoice.domain;

/** A cancel the status of the invoice forbids; the message names the rule, never a value. */
public class InvoiceNotCancellable extends RuntimeException {

    public InvoiceNotCancellable(String rule) {
        super(rule);
    }
}
