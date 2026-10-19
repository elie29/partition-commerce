package com.commerce.payment.application;

import com.commerce.payment.domain.CardToken;
import com.commerce.payment.domain.Money;

/** The port to CardPay, through which card payments go (DEC-1); an outbound adapter implements it. */
public interface CardPay {

    /** Charges the card the token stands for, for the amount, once: accepted with a charge reference, or declined. */
    Charge charge(CardToken cardToken, Money amount);
}
