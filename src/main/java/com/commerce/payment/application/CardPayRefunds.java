package com.commerce.payment.application;

import com.commerce.payment.domain.Money;

/**
 * The port to CardPay, through which refunds go (DEC-1, BR-28); an outbound adapter implements it. What follows a send
 * CardPay does not accept is an open question of the spec, until CardPay is reachable.
 */
public interface CardPayRefunds {

    /** Sends a refund of the amount to the card the charge reference names. */
    void send(String chargeReference, Money amount);
}
