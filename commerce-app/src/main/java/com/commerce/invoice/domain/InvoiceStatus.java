package com.commerce.invoice.domain;

/** The status of an invoice (P-1). */
public enum InvoiceStatus {
    /** Neither paid nor cancelled (BR-4). */
    ISSUED,
    /** Its payment is recorded, by {@code payment} (BR-23). */
    PAID,
    /** Voided before any payment, at no fee (BR-4). */
    CANCELLED
}
