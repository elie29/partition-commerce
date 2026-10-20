package com.commerce.product.inbound;

import com.commerce.product.application.CreatePriceListUseCase;
import com.commerce.product.application.Fault;
import com.commerce.product.application.SetPriceUseCase;
import com.commerce.product.inbound.api.model.CreatePriceListRequest;
import com.commerce.product.inbound.api.model.SetPriceRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The content of a body of {@code price-list}, judged whole before it is bound (AR-1: one item per field at fault in
 * the content): the contract's schema on the JSON tree, then what the use case judges, merged by field. A body that
 * is not JSON, or not an object, is one item with no field.
 */
@ControllerAdvice(assignableTypes = PriceListController.class)
class PriceListBodyCheck extends RequestBodyAdviceAdapter {

    private static final Pattern CURRENCY = Pattern.compile("[A-Z]{3}");
    private static final Set<String> CHANNELS = Set.of("SHOP", "MARKETPLACE", "B2B");

    private final JsonMapper json;
    private final CreatePriceListUseCase createPriceList;
    private final SetPriceUseCase setPrice;

    PriceListBodyCheck(JsonMapper json, CreatePriceListUseCase createPriceList, SetPriceUseCase setPrice) {
        this.json = json;
        this.createPriceList = createPriceList;
        this.setPrice = setPrice;
    }

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return parameter.getParameterType() == CreatePriceListRequest.class
                || parameter.getParameterType() == SetPriceRequest.class;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        byte[] body = input.getBody().readAllBytes();
        JsonNode tree;
        try {
            tree = json.readTree(body);
        } catch (JacksonException e) {
            throw new BodyUnreadable();
        }
        if (tree == null || !tree.isObject()) {
            throw new BodyUnreadable();
        }
        List<Fault> faults = parameter.getParameterType() == SetPriceRequest.class
                ? setPriceFaults(tree)
                : createPriceListFaults(tree);
        if (!faults.isEmpty()) {
            throw new RequestRefused(faults.stream().map(f -> new Fault("/" + f.field(), f.message())).toList());
        }
        HttpHeaders headers = input.getHeaders();
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }

    private List<Fault> createPriceListFaults(JsonNode tree) {
        JsonBody schema = new JsonBody();
        String name = schema.string(tree, "name", "name", true, null, 1, 80);
        String currency = schema.string(tree, "currency", "currency", true, CURRENCY, 0, Integer.MAX_VALUE);
        schema.oneOf(tree, "channel", "channel", true, CHANNELS);
        JsonNode validity = schema.object(tree, "validity", "validity", false);
        if (validity != null) {
            schema.date(validity, "from", "validity/from", true);
            schema.date(validity, "to", "validity/to", false);
        }
        String blankable = tree.path("name").isString() ? tree.get("name").asString() : null;
        return merged(schema.faults(), createPriceList.contentFaults(blankable, currency));
    }

    /** BR-10: the body is one group, every field at fault in it answered; BR-3 is checked after the list. */
    private List<Fault> setPriceFaults(JsonNode tree) {
        JsonBody schema = new JsonBody();
        JsonNode price = schema.object(tree, "price", "price", true);
        Long amount = null;
        String currency = null;
        if (price != null) {
            amount = schema.int64(price, "amountMinor", "price/amountMinor", true);
            currency = schema.string(price, "currency", "price/currency", true, CURRENCY, 0, Integer.MAX_VALUE);
        }
        schema.dateTime(tree, "effectiveFrom", "effectiveFrom", false);
        return merged(schema.faults(), setPrice.contentFaults(amount, currency));
    }

    private static List<Fault> merged(List<Fault> schema, List<Fault> useCase) {
        Map<String, Fault> byField = new LinkedHashMap<>();
        schema.forEach(f -> byField.putIfAbsent(f.field(), f));
        useCase.forEach(f -> byField.putIfAbsent(f.field(), f));
        return new ArrayList<>(byField.values());
    }
}
