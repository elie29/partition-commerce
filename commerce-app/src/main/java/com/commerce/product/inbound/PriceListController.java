package com.commerce.product.inbound;

import com.commerce.product.application.CreatePriceListUseCase;
import com.commerce.product.application.GetPriceListUseCase;
import com.commerce.product.application.GetProductPriceUseCase;
import com.commerce.product.application.PriceListView;
import com.commerce.product.application.PriceView;
import com.commerce.product.application.SetPriceUseCase;
import com.commerce.product.inbound.api.PriceListApi;
import com.commerce.product.inbound.api.model.CreatePriceListRequest;
import com.commerce.product.inbound.api.model.Money;
import com.commerce.product.inbound.api.model.Price;
import com.commerce.product.inbound.api.model.PriceList;
import com.commerce.product.inbound.api.model.SetPriceRequest;
import java.net.URI;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The operations of the tag {@code PriceList} of `docs/contracts/catalog.openapi.json`, under its base path. The
 * headers and the query are checked before binding ({@link PriceListRequestCheck}), the body whole before it is bound
 * ({@link PriceListBodyCheck}), then the path's identifiers. {@code Accept-Language} changes nothing (BR-22).
 */
@RestController
@RequestMapping(PriceListController.BASE_PATH)
public class PriceListController implements PriceListApi {

    static final String BASE_PATH = "/commerce/v1/catalog";

    private final CreatePriceListUseCase createPriceList;
    private final GetPriceListUseCase getPriceList;
    private final SetPriceUseCase setPrice;
    private final GetProductPriceUseCase getProductPrice;

    public PriceListController(
            CreatePriceListUseCase createPriceList,
            GetPriceListUseCase getPriceList,
            SetPriceUseCase setPrice,
            GetProductPriceUseCase getProductPrice) {
        this.createPriceList = createPriceList;
        this.getPriceList = getPriceList;
        this.setPrice = setPrice;
        this.getProductPrice = getProductPrice;
    }

    /** The request model prints its amount: it is handed to the use case and never logged (BR-28). */
    @Override
    public ResponseEntity<Price> setPrice(
            String xRequestId, String priceListId, String productId, SetPriceRequest request, String acceptLanguage) {
        OffsetDateTime effectiveFrom = request.getEffectiveFrom();
        PriceView set = setPrice.set(priceListId, productId, request.getPrice().getAmountMinor(),
                request.getPrice().getCurrency(), effectiveFrom == null ? null : effectiveFrom.toInstant());
        return ResponseEntity.ok(toPrice(set));
    }

    @Override
    public ResponseEntity<Price> getProductPrice(
            String xRequestId, String priceListId, String productId, String acceptLanguage, LocalDate at) {
        return ResponseEntity.ok(toPrice(getProductPrice.get(priceListId, productId, at)));
    }

    @Override
    public ResponseEntity<PriceList> createPriceList(
            String xRequestId, CreatePriceListRequest request, String acceptLanguage) {
        PriceListView created =
                createPriceList.create(request.getName(), request.getCurrency(), request.getChannel().getValue());
        return ResponseEntity.created(URI.create(BASE_PATH + "/price-lists/" + created.priceListId()))
                .body(toPriceList(created));
    }

    @Override
    public ResponseEntity<PriceList> getPriceList(String xRequestId, String priceListId, String acceptLanguage) {
        return ResponseEntity.ok(toPriceList(getPriceList.get(priceListId)));
    }

    private static Price toPrice(PriceView price) {
        return new Price(price.productId().toString(), new Money(price.amountMinor(), price.currency()))
                .effectiveFrom(price.effectiveFrom().atOffset(ZoneOffset.UTC));
    }

    private static PriceList toPriceList(PriceListView list) {
        return new PriceList(list.priceListId().toString(), list.name())
                .currency(list.currency())
                .channel(PriceList.ChannelEnum.fromValue(list.channel()));
    }
}
