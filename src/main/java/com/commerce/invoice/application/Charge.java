package com.commerce.invoice.application;

/** CardPay's answer to a charge. */
public sealed interface Charge {

    /** CardPay accepted the charge and answered its charge reference. */
    record Accepted(String chargeReference) implements Charge {
    }

    /** CardPay declined the card: no payment (BR-5). */
    record Declined() implements Charge {
    }
}
