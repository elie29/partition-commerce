package com.commerce.payment.inbound;

import com.commerce.payment.application.InvoiceToPay;
import com.commerce.payment.inbound.api.model.Invoice;
import com.commerce.payment.inbound.api.model.Line;
import com.commerce.payment.inbound.api.model.Money;

/** The contract's {@code Invoice} schema, as the pay answers it. */
final class PaymentModels {

    private PaymentModels() {
    }

    static Invoice toInvoice(InvoiceToPay invoice) {
        return new Invoice()
                .invoiceId(invoice.invoiceId().toString())
                .orderId(invoice.orderId())
                .status(Invoice.StatusEnum.fromValue(invoice.status()))
                .amount(new Money(invoice.amountMinor(), invoice.currency()))
                .lines(invoice.lines().stream()
                        .map(l -> new Line(l.label(), new Money(l.priceMinor(), l.currency())))
                        .toList());
    }
}
