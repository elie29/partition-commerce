package com.commerce.invoice.application;

/**
 * The content of a request breaks a business rule: of an issue (BR-7 to BR-12, BR-16), refused before BR-1 is checked
 * (BR-13); of a pay (BR-22), refused before the invoice is read.
 */
public class InvoiceContentRefused extends RuntimeException {

    public InvoiceContentRefused(String reason, Throwable cause) {
        super(reason, cause);
    }
}
