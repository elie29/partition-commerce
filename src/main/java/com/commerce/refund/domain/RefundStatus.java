package com.commerce.refund.domain;

/** Where a refund stands: requested, sent to the card through CardPay, settled by CardPay (REQ-6; P-2). */
public enum RefundStatus {
    REQUESTED,
    SENT,
    SETTLED
}
