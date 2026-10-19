package com.commerce.invoice.domain;

/** A line of an invoice: a label and a price; no quantity, no tax. */
public record Line(String label, Money price) {

    public Line {
        Text.require(label, "BR-12: a label is non-blank and holds at most 255 characters");
        if (price == null || price.isNegative()) {
            throw new InvalidInvoice("BR-8: a price is zero or more");
        }
    }
}
