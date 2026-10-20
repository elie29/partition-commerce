package com.commerce.discount.inbound;

import com.commerce.discount.application.CreateDiscountUseCase;
import com.commerce.discount.application.Fault;
import com.commerce.discount.inbound.api.model.CreateDiscountRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * The content of a body of {@code discount}, judged whole before it is bound (BR-9, AR-1: one item per field at fault
 * in the content): the contract's schema on the JSON tree, then what the use case judges, merged by field. A body that
 * is not JSON, or not an object, is one item with no field.
 */
@ControllerAdvice(assignableTypes = DiscountController.class)
class DiscountBodyCheck extends RequestBodyAdviceAdapter {

    private static final Pattern CURRENCY = Pattern.compile("[A-Z]{3}");

    private final JsonMapper json;
    private final CreateDiscountUseCase createDiscount;

    DiscountBodyCheck(JsonMapper json, CreateDiscountUseCase createDiscount) {
        this.json = json;
        this.createDiscount = createDiscount;
    }

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return parameter.getParameterType() == CreateDiscountRequest.class;
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
        List<Fault> faults = createDiscountFaults(tree);
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

    /** The period is judged by the schema only: accepted, not kept, an end before its start included (BR-2). */
    private List<Fault> createDiscountFaults(JsonNode tree) {
        JsonBody schema = new JsonBody();
        Long percent = schema.int64(tree, "percent", "percent", true);
        JsonNode minimumOrder = schema.object(tree, "minimumOrder", "minimumOrder", false);
        Long amount = null;
        String currency = null;
        if (minimumOrder != null) {
            amount = schema.int64(minimumOrder, "amountMinor", "minimumOrder/amountMinor", true);
            currency = schema.string(minimumOrder, "currency", "minimumOrder/currency", true, CURRENCY);
        }
        JsonNode period = schema.object(tree, "period", "period", false);
        if (period != null) {
            schema.dateTime(period, "start", "period/start", true);
            schema.dateTime(period, "end", "period/end", false);
        }
        return merged(schema.faults(), createDiscount.contentFaults(percent, amount, currency));
    }

    private static List<Fault> merged(List<Fault> schema, List<Fault> useCase) {
        Map<String, Fault> byField = new LinkedHashMap<>();
        schema.forEach(f -> byField.putIfAbsent(f.field(), f));
        useCase.forEach(f -> byField.putIfAbsent(f.field(), f));
        return new ArrayList<>(byField.values());
    }
}
