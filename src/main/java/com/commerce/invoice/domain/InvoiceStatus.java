package com.commerce.invoice.domain;

/** The status of an invoice (P-1); this version issues and cancels, and pays none. */
public enum InvoiceStatus {
    /** Neither paid nor cancelled (BR-4). */
    ISSUED,
    /** Voided before any payment, at no fee (BR-4). */
    CANCELLED
}
