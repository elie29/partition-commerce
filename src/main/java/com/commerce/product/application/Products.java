package com.commerce.product.application;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** The products of the store, the port {@code product}'s outbound adapter implements. */
public interface Products {

    /**
     * Records a new product; the store's unique constraint on the reference holds BR-1, so of two at the same moment
     * the first to commit holds.
     *
     * @throws ReferenceAlreadyHeld when another product holds the reference
     */
    void add(ProductView product);

    Optional<ProductView> find(UUID productId);

    /** The page {@code page} of {@code size} products, by reference ascending; no reference given is no filter. */
    ProductPage page(Collection<String> references, long page, int size);
}
