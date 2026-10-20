package com.commerce.discount.inbound;

import com.commerce.discount.application.Fault;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * The contract's schema of a body, read on its JSON tree before any binding, so that every field at fault in the
 * content is answered, a value of the wrong type included (AR-1). Field names are paths without the leading
 * {@code /}.
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

    /** The text at {@code field} matching {@code pattern}, or {@code null} when absent or at fault. */
    String string(JsonNode parent, String field, String path, boolean required, Pattern pattern) {
        String value = text(parent, field, path, required);
        if (value != null && !pattern.matcher(value).matches()) {
            faults.add(new Fault(path, "The value does not match the contract's pattern."));
            return null;
        }
        return value;
    }

    /** An integer of 64 bits, or {@code null} when absent or at fault. */
    Long int64(JsonNode parent, String field, String path, boolean required) {
        JsonNode node = present(parent, field, path, required);
        if (node == null) {
            return null;
        }
        if (!node.isIntegralNumber() || !node.canConvertToLong()) {
            faults.add(new Fault(path, "The value is not an integer."));
            return null;
        }
        return node.longValue();
    }

    /** A date-time carries its offset, as the schema's {@code date-time} requires. */
    void dateTime(JsonNode parent, String field, String path, boolean required) {
        String value = text(parent, field, path, required);
        if (value != null) {
            try {
                OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e) {
                faults.add(new Fault(path, "The value is not a date-time with its offset."));
            }
        }
    }

    private String text(JsonNode parent, String field, String path, boolean required) {
        JsonNode node = present(parent, field, path, required);
        if (node == null) {
            return null;
        }
        if (!node.isString()) {
            faults.add(new Fault(path, "The value is not a string."));
            return null;
        }
        return node.asString();
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
