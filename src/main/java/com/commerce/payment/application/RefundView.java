package com.commerce.payment.application;

import com.commerce.payment.domain.Refund;
import java.util.UUID;

/** A refund as a refund answers it. {@link #toString()} shows its identifiers and its status only (BR-31). */
public record RefundView(UUID refundId, UUID invoiceId, long amountMinor, String currency, String status) {

    static RefundView of(Refund refund) {
        return new RefundView(refund.id(), refund.invoiceId(), refund.amount().amountMinor(),
                refund.amount().currency(), refund.status().name());
    }

    @Override
    public String toString() {
        return "RefundView[" + refundId + ", invoice " + invoiceId + ", " + status + "]";
    }
}
