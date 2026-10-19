package com.commerce.payment.application;

import java.util.List;
import java.util.UUID;

/** An invoice as {@code invoice} shows it to {@code payment}: what is charged, and what a pay answers. */
public record InvoiceToPay(
        UUID invoiceId, String orderId, String status, long amountMinor, String currency, List<InvoiceLine> lines) {

    /** Only an invoice whose status is {@code ISSUED} is paid (BR-20). */
    boolean issued() {
        return "ISSUED".equals(status);
    }
}
