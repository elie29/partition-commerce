package com.commerce.payment.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * All or part of the amount paid of an invoice, given back to the card through CardPay (REQ-6). Its status changes
 * only through its guarded methods (P-2). {@link #toString()} shows its identifiers and its status only, never the
 * charge reference nor the amount (BR-31).
 */
public final class Refund {

    private final UUID id;
    private final UUID invoiceId;
    private final Money amount;
    private final String chargeReference;
    private final RefundStatus status;

    private Refund(UUID id, UUID invoiceId, Money amount, String chargeReference, RefundStatus status) {
        this.id = Objects.requireNonNull(id);
        this.invoiceId = Objects.requireNonNull(invoiceId);
        this.amount = Objects.requireNonNull(amount);
        this.chargeReference = Text.require(chargeReference, "AR-1: a charge reference is bounded by AR-1");
        this.status = Objects.requireNonNull(status);
    }

    /**
     * A refund requested against the amount paid of an invoice, in its currency (BR-25); whether it fits what remains
     * is checked when it is recorded.
     */
    public static Refund request(UUID invoiceId, Money amount, Money amountPaid, String chargeReference) {
        if (!amount.currency().equals(amountPaid.currency())) {
            throw new InvalidPayment("BR-25: a refund is in the currency of the amount paid");
        }
        return new Refund(UUID.randomUUID(), invoiceId, amount, chargeReference, RefundStatus.REQUESTED);
    }

    /** The refund once CardPay sent it to the card (BR-28); only a requested refund is sent. */
    public Refund sent() {
        if (status != RefundStatus.REQUESTED) {
            throw new InvalidPayment("BR-28: only a requested refund is sent");
        }
        return new Refund(id, invoiceId, amount, chargeReference, RefundStatus.SENT);
    }

    public UUID id() {
        return id;
    }

    public UUID invoiceId() {
        return invoiceId;
    }

    public Money amount() {
        return amount;
    }

    public String chargeReference() {
        return chargeReference;
    }

    public RefundStatus status() {
        return status;
    }

    @Override
    public String toString() {
        return "Refund[" + id + ", invoice " + invoiceId + ", " + status + "]";
    }
}
