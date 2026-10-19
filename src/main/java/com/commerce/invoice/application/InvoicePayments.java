package com.commerce.invoice.application;

import java.util.Optional;
import java.util.UUID;

/**
 * The contract through which {@code payment} reaches {@code invoice}, in process. Frozen on 2026-10-19 for the
 * fission of {@code invoice} (`docs/architecture/reshapes/2026-10-19-invoice-fission.md`): what it says changes only
 * through the architect. Every write of the invoice's state stays inside {@code invoice}.
 */
public interface InvoicePayments {

    /** The invoice as {@code getInvoice} shows it; empty when the identifier names no invoice. */
    Optional<InvoiceView> find(UUID invoiceId);

    /**
     * Records the invoice paid: its status {@code ISSUED} becomes {@code PAID}, and nothing else (BR-3), in the
     * caller's transaction.
     *
     * @return the paid invoice
     * @throws InvoiceNotIssued when the invoice is no longer {@code ISSUED}, a simultaneous pay or cancel included
     */
    InvoiceView recordPayment(UUID invoiceId);

    /**
     * Records the payment of a paid invoice cancelled: its status {@code PAID} becomes {@code ISSUED} again (REQ-5).
     * Built with REQ-5, whose refusals it then declares.
     *
     * @throws UnsupportedOperationException until REQ-5 is built
     */
    default InvoiceView recordPaymentCancelled(UUID invoiceId) {
        throw new UnsupportedOperationException("REQ-5: built with the cancel of a paid invoice");
    }
}
