package com.commerce.product.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Lists the products: BR-7 of the spec of {@code product}. */
@Service
public class ListProductsUseCase {

    private final Products products;

    public ListProductsUseCase(Products products) {
        this.products = products;
    }

    /**
     * References compared exactly, a reference given twice counted once; an empty value is no reference, and no
     * reference is no filter.
     */
    public ProductPage list(List<String> references, int page, int size) {
        Set<String> filter = new LinkedHashSet<>();
        if (references != null) {
            references.stream().filter(r -> r != null && !r.isEmpty()).forEach(filter::add);
        }
        return products.page(filter, page, size);
    }
}
