package com.commerce.product.application;

/**
 * One field at fault in a request, named as the contract names it ({@code label}, {@code attributes/gtin}), with one
 * sentence saying what is wrong and never the value sent (BR-27).
 */
public record Fault(String field, String message) {
}
