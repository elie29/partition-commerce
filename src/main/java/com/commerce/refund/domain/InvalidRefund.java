package com.commerce.refund.domain;

/** A refund whose content breaks a rule; the message names the rule, never a value (BR-31). */
public class InvalidRefund extends RuntimeException {

    public InvalidRefund(String rule) {
        super(rule);
    }
}
