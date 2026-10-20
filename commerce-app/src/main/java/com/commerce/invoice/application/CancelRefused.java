package com.commerce.invoice.application;

/** BR-4, BR-18: the invoice is paid or already cancelled, a simultaneous pay or cancel included. */
public class CancelRefused extends RuntimeException {

    public CancelRefused(String reason, Throwable cause) {
        super(reason, cause);
    }
}
