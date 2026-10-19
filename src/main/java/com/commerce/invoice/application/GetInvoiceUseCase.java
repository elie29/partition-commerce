package com.commerce.invoice.application;

import org.springframework.stereotype.Service;

/** Anyone holding the identifier of an invoice reads it back (BR-15). */
@Service
public class GetInvoiceUseCase {

    private final Invoices invoices;

    public GetInvoiceUseCase(Invoices invoices) {
        this.invoices = invoices;
    }

    /** @throws InvoiceNotFound when the identifier names no invoice, or is not a UUID */
    public InvoiceView read(String invoiceId) {
        return InvoiceIds.parse(invoiceId)
                .flatMap(invoices::find)
                .map(InvoiceView::of)
                .orElseThrow(InvoiceNotFound::new);
    }
}
