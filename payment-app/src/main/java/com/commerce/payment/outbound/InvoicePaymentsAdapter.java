package com.commerce.payment.outbound;

import com.commerce.payment.application.InvoiceLine;
import com.commerce.payment.application.InvoiceNoLongerIssued;
import com.commerce.payment.application.InvoiceToPay;
import com.commerce.payment.application.PayableInvoices;
import com.commerce.payment.outbound.internal.api.InvoicePaymentsApi;
import com.commerce.payment.outbound.internal.api.model.Invoice;
import com.commerce.payment.outbound.internal.api.model.InvoiceCustomer;
import com.commerce.payment.outbound.internal.api.model.Line;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * The port {@link PayableInvoices} over {@code invoice}'s operations of `docs/contracts/billing-internal.openapi.json`,
 * over HTTP (GoF Adapter): the only class of {@code payment} that knows {@code invoice}, and only through that
 * contract. A 404 is no invoice, a 409 of {@code recordPayment} an invoice no longer issued; any other failure reaches
 * the use case as it is thrown, 500.
 */
@Component
class InvoicePaymentsAdapter implements PayableInvoices {

    private final InvoicePaymentsApi invoicePayments;

    InvoicePaymentsAdapter(InvoicePaymentsApi invoicePayments) {
        this.invoicePayments = invoicePayments;
    }

    @Override
    public Optional<InvoiceToPay> find(UUID invoiceId) {
        try {
            return Optional.of(toPay(invoicePayments.getInvoiceForPayment(invoiceId.toString()).getBody()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> customerOf(UUID invoiceId) {
        try {
            InvoiceCustomer customer = invoicePayments.getInvoiceCustomer(invoiceId.toString()).getBody();
            return Optional.of(customer.getCustomerId());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public InvoiceToPay recordPaid(UUID invoiceId) {
        try {
            return toPay(invoicePayments.recordPayment(invoiceId.toString()).getBody());
        } catch (HttpClientErrorException.Conflict e) {
            throw new InvoiceNoLongerIssued(e);
        }
    }

    private static InvoiceToPay toPay(Invoice invoice) {
        return new InvoiceToPay(UUID.fromString(invoice.getInvoiceId()), invoice.getOrderId(),
                invoice.getStatus().getValue(), invoice.getAmount().getAmountMinor(),
                invoice.getAmount().getCurrency(),
                invoice.getLines().stream().map(InvoicePaymentsAdapter::toLine).toList());
    }

    private static InvoiceLine toLine(Line line) {
        return new InvoiceLine(line.getLabel(), line.getPrice().getAmountMinor(), line.getPrice().getCurrency());
    }
}
