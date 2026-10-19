package com.commerce.pricelist.application;

/**
 * One field at fault in a request, named as the contract names it ({@code name}, {@code price/currency}), with one
 * sentence saying what is wrong and never the value sent (AR-1).
 */
public record Fault(String field, String message) {
}
