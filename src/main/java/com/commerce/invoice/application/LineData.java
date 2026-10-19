package com.commerce.invoice.application;

/** A line as it crosses the application boundary, in and out. */
public record LineData(String label, long priceMinor, String currency) {
}
