package com.commerce.product.application;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The one definition in {@code product} of an identifier: a UUID in its 8-4-4-4-12 hexadecimal form (BR-29), which
 * {@link UUID#fromString} alone does not hold ({@code 1-1-1-1-1} passes it).
 */
public final class Identifiers {

    private static final Pattern FORM =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private Identifiers() {
    }

    public static boolean isUuid(String value) {
        return value != null && FORM.matcher(value).matches();
    }

    /** The identifier, or none when the text is not a UUID in its form. */
    public static Optional<UUID> parse(String value) {
        return isUuid(value) ? Optional.of(UUID.fromString(value)) : Optional.empty();
    }
}
