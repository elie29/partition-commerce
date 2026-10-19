package com.commerce.payment.outbound;

import com.commerce.invoice.application.InvoiceNotIssued;
import com.commerce.invoice.application.InvoicePayments;
import com.commerce.invoice.application.InvoiceView;
import com.commerce.invoice.application.LineData;
import com.commerce.payment.application.InvoiceLine;
import com.commerce.payment.application.InvoiceNoLongerIssued;
import com.commerce.payment.application.InvoiceToPay;
import com.commerce.payment.application.PayableInvoices;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * The port {@link PayableInvoices} over {@code invoice}'s contract {@link InvoicePayments}, in process (GoF Adapter):
 * the only class of {@code payment} that knows {@code invoice}, and only through that contract.
 */
@Component
class InvoicePaymentsAdapter implements PayableInvoices {

    private final InvoicePayments invoicePayments;

    InvoicePaymentsAdapter(InvoicePayments invoicePayments) {
        this.invoicePayments = invoicePayments;
    }

    @Override
    public Optional<InvoiceToPay> find(UUID invoiceId) {
        return invoicePayments.find(invoiceId).map(InvoicePaymentsAdapter::toPay);
    }

    @Override
    public InvoiceToPay recordPaid(UUID invoiceId) {
        try {
            return toPay(invoicePayments.recordPayment(invoiceId));
        } catch (InvoiceNotIssued e) {
            throw new InvoiceNoLongerIssued(e);
        }
    }

    private static InvoiceToPay toPay(InvoiceView view) {
        return new InvoiceToPay(view.invoiceId(), view.orderId(), view.status(), view.amountMinor(), view.currency(),
                view.lines().stream().map(InvoicePaymentsAdapter::toLine).toList());
    }

    private static InvoiceLine toLine(LineData line) {
        return new InvoiceLine(line.label(), line.priceMinor(), line.currency());
    }
}
