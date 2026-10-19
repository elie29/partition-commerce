package com.commerce.invoice.application;

import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.InvoiceNotCancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** The accountant cancels an invoice before any payment, at no fee (REQ-3); anyone holding its identifier may (BR-19). */
@Service
public class CancelInvoiceUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(CancelInvoiceUseCase.class);

    private final Invoices invoices;

    public CancelInvoiceUseCase(Invoices invoices) {
        this.invoices = invoices;
    }

    /**
     * @throws InvoiceNotFound when the identifier names no invoice, or is not a UUID (BR-19)
     * @throws CancelRefused when the invoice is already cancelled, a simultaneous cancel included (BR-18)
     */
    public InvoiceView cancel(String invoiceId) {
        Invoice invoice = InvoiceIds.parse(invoiceId)
                .flatMap(invoices::find)
                .orElseThrow(InvoiceNotFound::new);
        Invoice cancelled;
        try {
            cancelled = invoice.cancel();
        } catch (InvoiceNotCancellable e) {
            throw new CancelRefused(e.getMessage(), e);
        }
        if (!invoices.changeStatus(invoice.id(), invoice.status(), cancelled.status())) {
            throw new CancelRefused("BR-18: a simultaneous cancel came first", null);
        }
        LOG.info("Invoice {} cancelled for order {}", cancelled.id(), cancelled.orderId());
        return InvoiceView.of(cancelled);
    }
}
