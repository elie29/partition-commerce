package com.commerce.refund.application;

import java.util.Optional;
import java.util.UUID;

/** The port to the invoices {@code refund} refunds; an outbound adapter implements it over {@code payment}'s contract. */
public interface RefundableInvoices {

    /** The invoice; empty when the identifier names no invoice. */
    Optional<InvoiceToRefund> find(UUID invoiceId);
}
