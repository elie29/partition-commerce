package com.commerce.payment.application;

import java.util.Optional;
import java.util.UUID;

/**
 * The contract through which {@code refund} reaches {@code payment}, in process (architect, 2026-10-19): for any
 * invoice, whether it exists, its customer, and its payment when it is paid. Used from {@code refund}'s outbound
 * adapter only.
 */
public interface PaymentRefunds {

    /** The invoice as a refund needs it; empty when the identifier names no invoice. */
    Optional<RefundableInvoice> find(UUID invoiceId);
}
