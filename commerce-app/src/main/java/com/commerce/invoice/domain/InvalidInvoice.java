package com.commerce.invoice.domain;

/** An invoice whose content breaks a business rule of the spec; the message names the rule, never a value. */
public class InvalidInvoice extends RuntimeException {

    public InvalidInvoice(String rule) {
        super(rule);
    }
}
