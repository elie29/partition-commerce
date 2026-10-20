package com.commerce.discount.inbound;

import com.commerce.discount.application.CreateDiscountUseCase;
import com.commerce.discount.application.DiscountView;
import com.commerce.discount.application.GetDiscountUseCase;
import com.commerce.discount.application.MinimumOrder;
import com.commerce.discount.inbound.api.DiscountApi;
import com.commerce.discount.inbound.api.model.CreateDiscountRequest;
import com.commerce.discount.inbound.api.model.Discount;
import com.commerce.discount.inbound.api.model.Money;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The operations of the tag {@code Discount} of `docs/contracts/catalog.openapi.json`, under its base path. The
 * headers are checked before binding ({@link DiscountRequestCheck}), the body whole before it is bound
 * ({@link DiscountBodyCheck}). {@code Accept-Language} changes nothing (BR-10).
 */
@RestController
@RequestMapping(DiscountController.BASE_PATH)
public class DiscountController implements DiscountApi {

    static final String BASE_PATH = "/commerce/v1/catalog";

    private final CreateDiscountUseCase createDiscount;
    private final GetDiscountUseCase getDiscount;

    public DiscountController(CreateDiscountUseCase createDiscount, GetDiscountUseCase getDiscount) {
        this.createDiscount = createDiscount;
        this.getDiscount = getDiscount;
    }

    @Override
    public ResponseEntity<Discount> getDiscount(String xRequestId, String discountId, String acceptLanguage) {
        return ResponseEntity.ok(toDiscount(getDiscount.get(discountId)));
    }

    /** The contract declares no {@code Location} for a discount created. The request model is never logged (AR-2). */
    @Override
    public ResponseEntity<Discount> createDiscount(
            String xRequestId, CreateDiscountRequest request, String acceptLanguage) {
        Money sent = request.getMinimumOrder();
        DiscountView created = createDiscount.create(request.getPercent(),
                sent == null ? null : new MinimumOrder(sent.getAmountMinor(), sent.getCurrency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDiscount(created));
    }

    private static Discount toDiscount(DiscountView discount) {
        Discount answer = new Discount(discount.discountId().toString(), discount.percent());
        MinimumOrder minimumOrder = discount.minimumOrder();
        return minimumOrder == null
                ? answer
                : answer.minimumOrder(new Money(minimumOrder.amountMinor(), minimumOrder.currency()));
    }
}
