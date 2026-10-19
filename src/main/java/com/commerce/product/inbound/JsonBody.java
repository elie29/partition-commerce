package com.commerce.product.inbound;

import com.commerce.product.application.Fault;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * The contract's schema of a body, read on its JSON tree before any binding, so that every field at fault in the
 * content is answered, a value of the wrong type included (AR-1). A length counts Characters, code points
 * (`docs/specs/catalog/area.md`), as JSON Schema counts them. Field names are paths without the leading {@code /}.
 */
final class JsonBody {

    private final List<Fault> faults = new ArrayList<>();

    List<Fault> faults() {
        return faults;
    }

    /** The object at {@code field}, or {@code null} when absent or at fault. */
    JsonNode object(JsonNode parent, String field, String path, boolean required) {
        JsonNode node = present(parent, field, path, required);
        if (node != null && !node.isObject()) {
            faults.add(new Fault(path, "The value is not an object."));
            return null;
        }
        return node;
    }

    /** The array at {@code field}, or {@code null} when absent or at fault. */
    JsonNode array(JsonNode parent, String field, String path, boolean required) {
        JsonNode node = present(parent, field, path, required);
        if (node != null && !node.isArray()) {
            faults.add(new Fault(path, "The value is not an array."));
            return null;
        }
        return node;
    }

    /** The text at {@code field}, or {@code null} when absent or at fault. */
    String string(JsonNode parent, String field, String path, boolean required, Pattern pattern, int minLength,
            int maxLength) {
        JsonNode node = present(parent, field, path, required);
        if (node == null) {
            return null;
        }
        if (!node.isString()) {
            faults.add(new Fault(path, "The value is not a string."));
            return null;
        }
        String value = node.asString();
        int characters = value.codePointCount(0, value.length());
        if (characters < minLength) {
            faults.add(new Fault(path, "The value is blank."));
        } else if (characters > maxLength) {
            faults.add(new Fault(path, "The value holds more than " + maxLength + " characters."));
        } else if (pattern != null && !pattern.matcher(value).matches()) {
            faults.add(new Fault(path, "The value does not match the contract's pattern."));
        } else {
            return value;
        }
        return null;
    }

    /** An integer of the schema's type with no format, which the generated model holds in 32 bits. */
    void int32(JsonNode parent, String field, String path) {
        JsonNode node = present(parent, field, path, false);
        if (node != null && (!node.isIntegralNumber() || !node.canConvertToInt())) {
            faults.add(new Fault(path, "The value is not an integer."));
        }
    }

    private JsonNode present(JsonNode parent, String field, String path, boolean required) {
        JsonNode node = parent.get(field);
        if (node == null || node.isMissingNode()) {
            if (required) {
                faults.add(new Fault(path, "The value is required."));
            }
            return null;
        }
        if (node.isNull()) {
            faults.add(new Fault(path, required ? "The value is required." : "The value is null."));
            return null;
        }
        return node;
    }
}
