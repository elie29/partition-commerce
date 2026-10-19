package com.commerce.invoice.application;

import java.util.List;

/** What the accountant sends to issue an invoice; the order, its customer and its items come with the request. */
public record IssueInvoiceCommand(String orderId, String customerId, List<LineData> lines) {
}
