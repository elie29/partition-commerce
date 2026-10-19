package com.commerce.invoice.domain;

/** The status of an invoice (P-1). */
public enum InvoiceStatus {
    /** Neither paid nor cancelled (BR-4). */
    ISSUED,
    /** Its charge is recorded (BR-5). */
    PAID,
    /** Voided before any payment, at no fee (BR-4). */
    CANCELLED
}
