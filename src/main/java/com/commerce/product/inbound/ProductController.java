package com.commerce.product.inbound;

import com.commerce.product.application.CreateProductUseCase;
import com.commerce.product.application.GetProductPriceUseCase;
import com.commerce.product.application.GetProductUseCase;
import com.commerce.product.application.ListProductsUseCase;
import com.commerce.product.application.ProductPage;
import com.commerce.product.application.ProductPriceView;
import com.commerce.product.application.ProductView;
import com.commerce.product.inbound.api.ProductApi;
import com.commerce.product.inbound.api.model.CreateProductRequest;
import com.commerce.product.inbound.api.model.ListProducts200Response;
import com.commerce.product.inbound.api.model.ListProducts200ResponsePage;
import com.commerce.product.inbound.api.model.Money;
import com.commerce.product.inbound.api.model.Price;
import com.commerce.product.inbound.api.model.Product;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The operations of the tag {@code Product} of `docs/contracts/catalog.openapi.json`, under its base path. The headers
 * and the query are checked before binding ({@link ProductRequestCheck}), the body whole before it is bound
 * ({@link ProductBodyCheck}), then the path's identifier.
 * {@code Accept-Language} and {@code localized} change nothing (BR-6).
 */
@RestController
@RequestMapping(ProductController.BASE_PATH)
public class ProductController implements ProductApi {

    static final String BASE_PATH = "/commerce/v1/catalog";

    private final CreateProductUseCase createProduct;
    private final GetProductUseCase getProduct;
    private final ListProductsUseCase listProducts;
    private final GetProductPriceUseCase getProductPrice;

    public ProductController(
            CreateProductUseCase createProduct,
            GetProductUseCase getProduct,
            ListProductsUseCase listProducts,
            GetProductPriceUseCase getProductPrice) {
        this.createProduct = createProduct;
        this.getProduct = getProduct;
        this.listProducts = listProducts;
        this.getProductPrice = getProductPrice;
    }

    /** The body was judged whole before it was bound ({@link ProductBodyCheck}). */
    @Override
    public ResponseEntity<Product> createProduct(
            String xRequestId, CreateProductRequest request, String acceptLanguage) {
        ProductView created = createProduct.create(request.getReference(), request.getLabel());
        return ResponseEntity.created(URI.create(BASE_PATH + "/products/" + created.productId()))
                .body(toProduct(created));
    }

    @Override
    public ResponseEntity<Product> getProduct(String xRequestId, String productId, String acceptLanguage) {
        return ResponseEntity.ok(toProduct(getProduct.get(productId)));
    }

    @Override
    public ResponseEntity<Price> getProductPrice(
            String xRequestId, String productId, String priceListId, String acceptLanguage, LocalDate at) {
        ProductPriceView price = getProductPrice.get(productId, priceListId, at);
        return ResponseEntity.ok(new Price(productId, new Money(price.amountMinor(), price.currency()))
                .effectiveFrom(price.effectiveFrom().atOffset(ZoneOffset.UTC)));
    }

    @Override
    public ResponseEntity<ListProducts200Response> listProducts(
            String xRequestId,
            String acceptLanguage,
            List<String> references,
            Boolean localized,
            Integer page,
            Integer size) {
        ProductPage found = listProducts.list(references, page, size);
        long totalPages = (found.totalElements() + size - 1) / size;
        ListProducts200ResponsePage pageOf = new ListProducts200ResponsePage(
                page, size, found.totalElements(), (int) Math.min(totalPages, Integer.MAX_VALUE));
        return ResponseEntity.ok(new ListProducts200Response(
                found.items().stream().map(ProductController::toProduct).toList(), pageOf));
    }

    private static Product toProduct(ProductView product) {
        return new Product(product.productId().toString(), product.reference(), product.label())
                .createdAt(product.createdAt().atOffset(ZoneOffset.UTC));
    }
}
