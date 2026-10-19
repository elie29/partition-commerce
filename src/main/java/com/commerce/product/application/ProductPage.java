package com.commerce.product.application;

import java.util.List;

/** One page of products, by reference ascending, and the number of products the filter matches (BR-7). */
public record ProductPage(List<ProductView> items, long totalElements) {
}
