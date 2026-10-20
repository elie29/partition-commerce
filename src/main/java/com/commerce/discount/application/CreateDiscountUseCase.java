package com.commerce.discount.application;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Creates a discount: BR-1 to BR-5 and BR-7 of the spec of {@code discount}. Two discounts may overlap, a retry
 * creates another one: it never answers 409 (BR-7).
 */
@Service
public class CreateDiscountUseCase {

    private static final int LOWEST_PERCENT = 1;
    private static final int HIGHEST_PERCENT = 90;

    private final Discounts discounts;

    public CreateDiscountUseCase(Discounts discounts) {
        this.discounts = discounts;
    }

    /**
     * The faults of the content this use case judges, beyond the contract's schema: a percent outside 1 to 90 (BR-1),
     * a negative minimum order (BR-3), a currency that is not a known code (BR-4). A value of the wrong type is passed
     * as {@code null} and judged by the schema only; the caller answers both in one 400 (BR-9).
     */
    public List<Fault> contentFaults(Long percent, Long amountMinor, String currency) {
        List<Fault> faults = new ArrayList<>();
        if (percent != null && (percent < LOWEST_PERCENT || percent > HIGHEST_PERCENT)) {
            faults.add(new Fault("percent", "The percent is not between 1 and 90."));
        }
        if (amountMinor != null && amountMinor < 0) {
            faults.add(new Fault("minimumOrder/amountMinor", "The amount is negative."));
        }
        if (currency != null && !KnownCurrency.is(currency)) {
            faults.add(new Fault("minimumOrder/currency", "The currency is not a known ISO 4217 code."));
        }
        return faults;
    }

    /**
     * A content {@link #contentFaults} and the contract accept; {@code minimumOrder} is {@code null} when none was
     * sent. The period is accepted, not kept (BR-2).
     */
    public DiscountView create(int percent, MinimumOrder minimumOrder) {
        DiscountView discount = new DiscountView(UUID.randomUUID(), percent, minimumOrder);
        discounts.add(discount);
        return discount;
    }
}
