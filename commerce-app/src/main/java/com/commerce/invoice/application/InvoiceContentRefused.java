package com.commerce.invoice.application;

/** The content of an issue breaks a business rule (BR-7 to BR-12, BR-16), refused before BR-1 is checked (BR-13). */
public class InvoiceContentRefused extends RuntimeException {

    public InvoiceContentRefused(String reason, Throwable cause) {
        super(reason, cause);
    }
}
