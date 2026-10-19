package com.commerce.invoice.application;

/** BR-1: an invoice is issued once per order. */
public class OrderAlreadyInvoiced extends RuntimeException {

    public OrderAlreadyInvoiced(Throwable cause) {
        super("BR-1: an invoice is issued once per order", cause);
    }
}
