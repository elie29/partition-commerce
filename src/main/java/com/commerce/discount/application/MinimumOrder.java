package com.commerce.discount.application;

/**
 * The minimum order of a discount: an amount in minor units and its currency (INV-DATA-001); kept and returned, it
 * governs nothing in this version (BR-5).
 */
public record MinimumOrder(long amountMinor, String currency) {

    /** An amount is never written to a log (AR-2). */
    @Override
    public String toString() {
        return "MinimumOrder";
    }
}
