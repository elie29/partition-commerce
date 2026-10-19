package com.commerce.product.outbound;

import com.commerce.pricelist.application.PriceListPrices;
import com.commerce.product.application.ProductPriceView;
import com.commerce.product.application.ProductPrices;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * {@link ProductPrices} over {@code price-list}'s {@link PriceListPrices}: the only class of {@code product} that
 * imports {@code price-list}, and it uses nothing else of it (GoF Adapter, as {@code InvoicePaymentsAdapter}).
 */
@Component
class PriceListPricesAdapter implements ProductPrices {

    private final PriceListPrices priceListPrices;

    PriceListPricesAdapter(PriceListPrices priceListPrices) {
        this.priceListPrices = priceListPrices;
    }

    @Override
    public Optional<ProductPriceView> priceOn(UUID productId, UUID priceListId, LocalDate day) {
        return priceListPrices.priceOn(productId, priceListId, day)
                .map(price -> new ProductPriceView(price.amountMinor(), price.currency(), price.effectiveFrom()));
    }
}
