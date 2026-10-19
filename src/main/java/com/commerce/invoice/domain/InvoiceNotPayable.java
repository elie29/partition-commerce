package com.commerce.invoice.domain;

/** A record of a payment the status of the invoice forbids (BR-23); the message names the rule, never a value. */
public class InvoiceNotPayable extends RuntimeException {

    public InvoiceNotPayable(String rule) {
        super(rule);
    }
}
