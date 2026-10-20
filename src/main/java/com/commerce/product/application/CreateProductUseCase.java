package com.commerce.product.application;

import com.commerce.product.domain.Blank;
import com.commerce.product.domain.Product;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Creates a product: BR-1 to BR-5 of the spec of {@code product}. */
@Service
public class CreateProductUseCase {

    private final Products products;
    private final Clock clock;

    public CreateProductUseCase(Products products, Clock clock) {
        this.products = products;
        this.clock = clock;
    }

    /**
     * The faults of the content this use case judges, beyond the contract's schema: a blank label (BR-3). The caller
     * answers them with the schema's in one 400 (AR-1: one item per field at fault within the content).
     */
    public List<Fault> contentFaults(String label) {
        return label != null && Blank.is(label) ? List.of(new Fault("label", "The label is blank.")) : List.of();
    }

    /** A content {@link #contentFaults} and the contract accept; created at the server's clock, in UTC, to the ms. */
    public ProductView create(String reference, String label) {
        Instant now = clock.instant().truncatedTo(ChronoUnit.MILLIS);
        Product product = Product.create(UUID.randomUUID(), reference, label, now);
        products.add(product);
        return ProductView.of(product);
    }
}
