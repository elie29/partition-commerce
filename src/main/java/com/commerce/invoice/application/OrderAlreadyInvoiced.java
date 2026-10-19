package com.commerce.invoice.application;

/** BR-1: an order has at most one invoice that is not cancelled. */
public class OrderAlreadyInvoiced extends RuntimeException {

    public OrderAlreadyInvoiced(Throwable cause) {
        super("BR-1: an order has at most one invoice that is not cancelled", cause);
    }
}
