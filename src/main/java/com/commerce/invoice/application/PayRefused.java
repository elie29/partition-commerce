package com.commerce.invoice.application;

/** BR-20: the invoice is paid or cancelled, a simultaneous pay or cancel included (BR-5). */
public class PayRefused extends RuntimeException {

    public PayRefused(String reason, Throwable cause) {
        super(reason, cause);
    }
}
