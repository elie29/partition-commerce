package com.commerce.refund.application;

import com.commerce.refund.domain.Money;
import java.util.Optional;
import java.util.UUID;

/**
 * An invoice as {@code payment} shows it to {@code refund}: its customer, and, when it is paid, the amount paid and
 * the charge reference of its payment. {@link #toString()} names its identifier only (BR-31).
 */
public record InvoiceToRefund(UUID invoiceId, String customerId, Optional<Paid> payment) {

    /** The payment of a paid invoice: the amount paid and its charge reference. */
    public record Paid(Money amountPaid, String chargeReference) {

        @Override
        public String toString() {
            return "Paid[hidden]";
        }
    }

    @Override
    public String toString() {
        return "InvoiceToRefund[" + invoiceId + "]";
    }
}
