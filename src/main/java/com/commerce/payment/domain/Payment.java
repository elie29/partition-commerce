package com.commerce.payment.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * A card charge CardPay accepted, against one invoice (area language), with a key of its own. It records the amount
 * charged and the charge reference, never the card token (BR-24); its time is the moment its record commits.
 */
public record Payment(UUID id, UUID invoiceId, Money amount, String chargeReference) {

    public Payment {
        Objects.requireNonNull(id);
        Objects.requireNonNull(invoiceId);
        Objects.requireNonNull(amount);
        Text.require(chargeReference, "BR-5: a charge reference is bounded by AR-1");
    }

    /** The payment of the whole amount of an invoice, once CardPay accepted its charge (BR-5). */
    public static Payment of(UUID invoiceId, Money amount, String chargeReference) {
        return new Payment(UUID.randomUUID(), invoiceId, amount, chargeReference);
    }
}
