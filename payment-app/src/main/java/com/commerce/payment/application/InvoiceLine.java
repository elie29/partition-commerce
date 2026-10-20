package com.commerce.payment.application;

/** A line of an invoice to pay, as {@code invoice} shows it. */
public record InvoiceLine(String label, long priceMinor, String currency) {
}
