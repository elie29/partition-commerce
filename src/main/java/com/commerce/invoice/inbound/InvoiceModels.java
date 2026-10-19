package com.commerce.invoice.inbound;

import com.commerce.invoice.application.InvoiceView;
import com.commerce.invoice.inbound.api.model.Invoice;
import com.commerce.invoice.inbound.api.model.Line;
import com.commerce.invoice.inbound.api.model.Money;

/** The contract's {@code Invoice} schema, as the controllers of {@code invoice} answer it. */
final class InvoiceModels {

    private InvoiceModels() {
    }

    static Invoice toInvoice(InvoiceView view) {
        return new Invoice()
                .invoiceId(view.invoiceId().toString())
                .orderId(view.orderId())
                .status(Invoice.StatusEnum.fromValue(view.status()))
                .amount(new Money(view.amountMinor(), view.currency()))
                .lines(view.lines().stream()
                        .map(l -> new Line(l.label(), new Money(l.priceMinor(), l.currency())))
                        .toList());
    }
}
