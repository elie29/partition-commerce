package com.commerce.payment.application;

import java.util.Optional;
import java.util.UUID;

/** The port to the invoices {@code payment} pays; an outbound adapter implements it over {@code invoice}'s contract. */
public interface PayableInvoices {

    /** The invoice; empty when the identifier names no invoice. */
    Optional<InvoiceToPay> find(UUID invoiceId);

    /** The customer of the invoice, for the fraud review (AR-3); empty when the identifier names no invoice. */
    Optional<String> customerOf(UUID invoiceId);

    /**
     * Has the invoice recorded paid by {@code invoice}, which decides (BR-5).
     *
     * @return the paid invoice
     * @throws InvoiceNoLongerIssued when the invoice is no longer {@code ISSUED}
     */
    InvoiceToPay recordPaid(UUID invoiceId);
}
