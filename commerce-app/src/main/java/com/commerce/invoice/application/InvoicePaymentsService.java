package com.commerce.invoice.application;

import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.InvoiceNotPayable;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one adapter of {@link InvoicePayments}, over the port {@link Invoices}. The payment is recorded through the
 * aggregate (P-1), then by a conditional change of status, as a cancel is: of a pay and a cancel of one invoice at the
 * same moment, the first to commit holds (BR-23).
 */
@Service
class InvoicePaymentsService implements InvoicePayments {

    private final Invoices invoices;

    InvoicePaymentsService(Invoices invoices) {
        this.invoices = invoices;
    }

    @Override
    public Optional<InvoiceView> find(UUID invoiceId) {
        return invoices.find(invoiceId).map(InvoiceView::of);
    }

    @Override
    public Optional<String> customerOf(UUID invoiceId) {
        return invoices.find(invoiceId).map(Invoice::customerId);
    }

    @Override
    @Transactional
    public InvoiceView recordPayment(UUID invoiceId) {
        Invoice invoice = invoices.find(invoiceId).orElseThrow(InvoiceNotIssued::new);
        Invoice paid;
        try {
            paid = invoice.recordPayment();
        } catch (InvoiceNotPayable e) {
            throw new InvoiceNotIssued();
        }
        if (!invoices.changeStatus(invoiceId, invoice.status(), paid.status())) {
            throw new InvoiceNotIssued();
        }
        return InvoiceView.of(paid);
    }
}
