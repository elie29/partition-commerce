package com.commerce.product.application;

import com.commerce.product.domain.Currency;
import com.commerce.product.domain.Money;
import com.commerce.product.domain.Price;
import com.commerce.product.domain.PriceList;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sets the price of a product in a price list: BR-10 to BR-13, BR-17 to BR-19 and BR-21 of the spec of
 * {@code product}. The order of BR-19: the content ({@link #contentFaults}, before the call), then the list, then
 * the product identifier, then the list's currency.
 */
@Service
public class SetPriceUseCase {

    private final PriceLists priceLists;
    private final Prices prices;
    private final Clock clock;

    public SetPriceUseCase(PriceLists priceLists, Prices prices, Clock clock) {
        this.priceLists = priceLists;
        this.prices = prices;
        this.clock = clock;
    }

    /**
     * The faults of the content the domain judges, beyond the contract's schema: a negative amount (BR-11), a currency
     * that is not a known code (BR-13). A value of the wrong type is passed as {@code null} and judged by the schema.
     */
    public List<Fault> contentFaults(Long amountMinor, String currency) {
        List<Fault> faults = new ArrayList<>();
        if (amountMinor != null && !Money.isAmount(amountMinor)) {
            faults.add(new Fault("price/amountMinor", "The amount is negative."));
        }
        if (currency != null && !Currency.isKnown(currency)) {
            faults.add(new Fault("price/currency", "The currency is not a known ISO 4217 code."));
        }
        return faults;
    }

    /**
     * A content {@link #contentFaults} and the contract accept. The product is not read: a price is set for the
     * identifier given (BR-21). {@code effectiveFrom} absent is now; kept to the millisecond, in UTC (BR-17, BR-26).
     *
     * @throws PriceListNotFound when an identifier of the path is not a UUID or the list does not exist
     * @throws PriceRefused when the currency is not the list's (BR-12)
     */
    @Transactional
    public PriceView set(String priceListId, String productId, long amountMinor, String currency,
            Instant effectiveFrom) {
        PriceList list = Identifiers.parse(priceListId).flatMap(priceLists::find).orElseThrow(PriceListNotFound::new);
        UUID product = Identifiers.parse(productId).orElseThrow(PriceListNotFound::new);
        Currency of = Currency.of(currency);
        if (!list.isInItsCurrency(of)) {
            throw new PriceRefused(List.of(new Fault("price/currency", "The currency is not the price list's.")));
        }
        Instant from = (effectiveFrom == null ? clock.instant() : effectiveFrom).truncatedTo(ChronoUnit.MILLIS);
        Price price = list.price(product, new Money(amountMinor, of), from);
        prices.set(price);
        return PriceView.of(price);
    }
}
