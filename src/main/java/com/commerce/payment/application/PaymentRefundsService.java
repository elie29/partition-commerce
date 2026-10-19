package com.commerce.payment.application;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The one implementation of {@link PaymentRefunds}: the invoice and its customer through {@link PayableInvoices}
 * (over {@code invoice}'s {@code InvoicePayments}), its payment through {@link Payments}, shown only while the
 * invoice is {@code PAID}.
 */
@Service
class PaymentRefundsService implements PaymentRefunds {

    private final PayableInvoices invoices;
    private final Payments payments;

    PaymentRefundsService(PayableInvoices invoices, Payments payments) {
        this.invoices = invoices;
        this.payments = payments;
    }

    @Override
    public Optional<RefundableInvoice> find(UUID invoiceId) {
        return invoices.find(invoiceId).map(invoice -> new RefundableInvoice(
                invoiceId,
                invoices.customerOf(invoiceId).orElseThrow(),
                "PAID".equals(invoice.status())
                        ? payments.findOf(invoiceId).map(p -> new PaidPayment(
                                p.chargeReference(), p.amount().amountMinor(), p.amount().currency()))
                        : Optional.empty()));
    }
}
