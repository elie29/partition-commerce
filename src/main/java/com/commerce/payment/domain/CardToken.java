package com.commerce.payment.domain;

/**
 * What CardPay makes on the customer's side to stand for a card, never a card number; bounded by AR-1 (BR-22). It
 * appears in no log, no error and no trace (BR-24): {@link #toString()} hides it.
 */
public record CardToken(String value) {

    public CardToken {
        Text.require(value, "BR-22: a card token is bounded by AR-1");
    }

    @Override
    public String toString() {
        return "CardToken[hidden]";
    }
}
