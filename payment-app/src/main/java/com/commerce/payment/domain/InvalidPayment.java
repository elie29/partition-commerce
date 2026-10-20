package com.commerce.payment.domain;

/** A payment whose content breaks a rule; the message names the rule, never a value. */
public class InvalidPayment extends RuntimeException {

    public InvalidPayment(String rule) {
        super(rule);
    }
}
