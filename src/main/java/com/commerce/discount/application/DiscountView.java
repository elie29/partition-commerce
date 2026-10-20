package com.commerce.discount.application;

import java.util.UUID;

/**
 * A discount as it is kept and answered: its identifier, its percent, and its minimum order or {@code null} when none
 * was sent (BR-5). Its period is not kept (BR-2).
 */
public record DiscountView(UUID discountId, int percent, MinimumOrder minimumOrder) {

    /** Neither the percent nor an amount is written to a log (AR-2). */
    @Override
    public String toString() {
        return "DiscountView[" + discountId + "]";
    }
}
