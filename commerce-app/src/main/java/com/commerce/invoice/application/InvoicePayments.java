package com.commerce.invoice.application;

import java.util.Optional;
import java.util.UUID;

/**
 * The contract through which {@code payment} reaches {@code invoice}, served over HTTP to {@code payment-app} as the
 * tag {@code InvoicePayments} of `docs/contracts/billing-internal.openapi.json` 1.0.0, frozen for the promotion of
 * {@code payment} (`docs/architecture/reshapes/2026-10-20-payment-promotion.md`): what it says changes only through
 * the architect. Every write of the invoice's state stays inside {@code invoice}.
 */
public interface InvoicePayments {

    /** The invoice as {@code getInvoice} shows it; empty when the identifier names no invoice. */
    Optional<InvoiceView> find(UUID invoiceId);

    /**
     * The customer of the invoice, its {@code customerId}, for the fraud review (AR-3), which {@link #find} does not
     * show; empty when the identifier names no invoice. Added 2026-10-19, agreed by {@code invoice} and {@code payment}
     * (architect).
     */
    Optional<String> customerOf(UUID invoiceId);

    /**
     * Records the invoice paid: its status {@code ISSUED} becomes {@code PAID}, and nothing else (BR-3); {@code invoice}
     * decides, the first it records holds (BR-23).
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
