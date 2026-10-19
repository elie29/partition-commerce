package com.commerce.invoice.application;

import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.InvoiceStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The one adapter of {@link InvoicePayments}, over the port {@link Invoices}. The payment is recorded by a conditional
 * change of status, as a cancel is: of a pay and a cancel of one invoice at the same moment, the first to commit holds.
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
    @Transactional
    public InvoiceView recordPayment(UUID invoiceId) {
        Invoice issued = invoices.find(invoiceId)
                .filter(invoice -> invoice.status() == InvoiceStatus.ISSUED)
                .orElseThrow(InvoiceNotIssued::new);
        if (!invoices.changeStatus(invoiceId, InvoiceStatus.ISSUED, InvoiceStatus.PAID)) {
            throw new InvoiceNotIssued();
        }
        InvoiceView view = InvoiceView.of(issued);
        return new InvoiceView(view.invoiceId(), view.orderId(), InvoiceStatus.PAID.name(), view.amountMinor(),
                view.currency(), view.lines());
    }
}
