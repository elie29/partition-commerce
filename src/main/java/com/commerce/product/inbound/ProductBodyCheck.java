package com.commerce.product.inbound;

import com.commerce.product.application.CreateProductUseCase;
import com.commerce.product.application.Fault;
import com.commerce.product.inbound.api.model.CreateProductRequest;
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
 * The content of the body of {@code createProduct}, judged whole before it is bound (BR-27: one item per field at fault
 * in the content): the contract's schema on the JSON tree, then what the use case judges (BR-3), merged by field. A
 * body that is not JSON, or not an object, is one item with no field.
 */
@ControllerAdvice(assignableTypes = ProductController.class)
class ProductBodyCheck extends RequestBodyAdviceAdapter {

    private static final Pattern REFERENCE = Pattern.compile("[A-Z]{3}-[0-9]{6}");
    private static final Pattern GTIN = Pattern.compile("[0-9]{8,14}");
    private static final Pattern LANGUAGE = Pattern.compile("[a-z]{2}(-[A-Z]{2})?");
    private static final int UNBOUNDED = Integer.MAX_VALUE;

    private final JsonMapper json;
    private final CreateProductUseCase createProduct;

    ProductBodyCheck(JsonMapper json, CreateProductUseCase createProduct) {
        this.json = json;
        this.createProduct = createProduct;
    }

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        return parameter.getParameterType() == CreateProductRequest.class;
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
        List<Fault> faults = faults(tree);
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

    private List<Fault> faults(JsonNode tree) {
        JsonBody schema = new JsonBody();
        schema.string(tree, "reference", "reference", true, REFERENCE, 0, UNBOUNDED);
        schema.string(tree, "label", "label", true, null, 1, 120);
        schema.string(tree, "description", "description", false, null, 0, UNBOUNDED);
        schema.string(tree, "ean", "ean", false, null, 0, UNBOUNDED);
        JsonNode attributes = schema.object(tree, "attributes", "attributes", false);
        if (attributes != null) {
            schema.string(attributes, "brand", "attributes/brand", false, null, 0, UNBOUNDED);
            schema.string(attributes, "gtin", "attributes/gtin", false, GTIN, 0, UNBOUNDED);
            JsonNode dimensions = schema.object(attributes, "dimensions", "attributes/dimensions", false);
            if (dimensions != null) {
                for (String dimension : List.of("widthMm", "heightMm", "depthMm", "weightG")) {
                    schema.int32(dimensions, dimension, "attributes/dimensions/" + dimension);
                }
            }
            JsonNode translations = schema.array(attributes, "translations", "attributes/translations", false);
            for (int i = 0; translations != null && i < translations.size(); i++) {
                String at = "attributes/translations/" + i;
                JsonNode translation = translations.get(i);
                if (!translation.isObject()) {
                    schema.faults().add(new Fault(at, "The value is not an object."));
                    continue;
                }
                schema.string(translation, "language", at + "/language", true, LANGUAGE, 0, UNBOUNDED);
                schema.string(translation, "label", at + "/label", true, null, 0, UNBOUNDED);
            }
        }
        String label = tree.path("label").isString() ? tree.get("label").asString() : null;
        Map<String, Fault> byField = new LinkedHashMap<>();
        schema.faults().forEach(f -> byField.putIfAbsent(f.field(), f));
        createProduct.contentFaults(label).forEach(f -> byField.putIfAbsent(f.field(), f));
        return new ArrayList<>(byField.values());
    }
}
