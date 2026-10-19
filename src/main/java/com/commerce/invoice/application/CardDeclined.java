package com.commerce.invoice.application;

/** BR-5: CardPay declined the card; no payment, the invoice stays issued. */
public class CardDeclined extends RuntimeException {

    public CardDeclined() {
        super("BR-5: CardPay declined the card");
    }
}
