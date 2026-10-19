package com.commerce.invoice.domain;

/**
 * What CardPay makes on the customer's side to stand for a card, never a card number; bounded as a label is (BR-22,
 * BR-12). It appears in no log, no error and no trace (BR-3): {@link #toString()} hides it.
 */
public record CardToken(String value) {

    public CardToken {
        Text.require(value, "BR-22: a card token is bounded as a label is (BR-12)");
    }

    @Override
    public String toString() {
        return "CardToken[hidden]";
    }
}
