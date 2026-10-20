package com.commerce.invoice.application;

import com.commerce.invoice.domain.Invoice;
import java.util.List;
import java.util.UUID;

/** An invoice as a reader sees it: without its customer. */
public record InvoiceView(
        UUID invoiceId, String orderId, String status, long amountMinor, String currency, List<LineData> lines) {

    static InvoiceView of(Invoice invoice) {
        return new InvoiceView(
                invoice.id(),
                invoice.orderId(),
                invoice.status().name(),
                invoice.amount().amountMinor(),
                invoice.amount().currency(),
                invoice.lines().stream()
                        .map(l -> new LineData(l.label(), l.price().amountMinor(), l.price().currency()))
                        .toList());
    }
}
