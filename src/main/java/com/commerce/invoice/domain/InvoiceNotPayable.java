package com.commerce.invoice.domain;

/** A pay the status of the invoice forbids; the message names the rule, never a value. */
public class InvoiceNotPayable extends RuntimeException {

    public InvoiceNotPayable(String rule) {
        super(rule);
    }
}
