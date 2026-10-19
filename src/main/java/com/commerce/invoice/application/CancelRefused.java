package com.commerce.invoice.application;

/** BR-18: the invoice is already cancelled, a simultaneous cancel included. */
public class CancelRefused extends RuntimeException {

    public CancelRefused(String reason, Throwable cause) {
        super(reason, cause);
    }
}
