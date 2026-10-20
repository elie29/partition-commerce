package com.commerce.fraud.domain;

/** A {@code customerId} BR-7 refuses; its message names the rule, never the text (BR-9). */
public class InvalidCustomerId extends RuntimeException {

    InvalidCustomerId() {
        super("BR-7: a customerId is non-blank and holds at most 255 characters");
    }
}
