package com.commerce.payment.application;

import java.util.Optional;
import java.util.UUID;

/**
 * An invoice as {@code payment} shows it to {@code refund} ({@link PaymentRefunds}): its customer, and its payment
 * when its status is {@code PAID}, empty otherwise. {@link #toString()} names its identifier only.
 */
public record RefundableInvoice(UUID invoiceId, String customerId, Optional<PaidPayment> payment) {

    @Override
    public String toString() {
        return "RefundableInvoice[" + invoiceId + "]";
    }
}
