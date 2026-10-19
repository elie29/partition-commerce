package com.commerce.payment.application;

/** {@code invoice} refused to record the payment: the invoice is no longer {@code ISSUED}, a simultaneous pay or cancel included. */
public class InvoiceNoLongerIssued extends RuntimeException {

    public InvoiceNoLongerIssued(Throwable cause) {
        super("the invoice is no longer ISSUED", cause);
    }
}
