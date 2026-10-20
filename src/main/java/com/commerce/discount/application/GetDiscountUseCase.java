package com.commerce.discount.application;

import org.springframework.stereotype.Service;

/** Reads a discount: BR-8 of the spec of {@code discount}. */
@Service
public class GetDiscountUseCase {

    private final Discounts discounts;

    public GetDiscountUseCase(Discounts discounts) {
        this.discounts = discounts;
    }

    /** @throws DiscountNotFound when the identifier is not a UUID (AR-3) or names no discount */
    public DiscountView get(String discountId) {
        return Identifiers.parse(discountId)
                .flatMap(discounts::find)
                .orElseThrow(DiscountNotFound::new);
    }
}
