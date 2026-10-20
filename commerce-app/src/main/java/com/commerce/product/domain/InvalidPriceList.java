package com.commerce.product.domain;

/** A rule of the spec of {@code price-list} the domain refuses; names the rule, never a value (BR-28). */
public class InvalidPriceList extends RuntimeException {

    public InvalidPriceList(String rule) {
        super(rule);
    }
}
