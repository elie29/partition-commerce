package com.commerce.refund.application;

/** The content of a refund breaks BR-26, or its currency is not the amount paid's (BR-25): 400. */
public class RefundContentRefused extends RuntimeException {

    public RefundContentRefused(String reason) {
        super(reason);
    }
}
