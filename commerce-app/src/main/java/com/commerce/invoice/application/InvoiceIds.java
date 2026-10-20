package com.commerce.invoice.application;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** The one reading of an invoice identifier from a caller, for every use case that takes one (BR-15, BR-19). */
final class InvoiceIds {

    /** The canonical form only: {@link UUID#fromString} would also take {@code 1-1-1-1-1}. */
    private static final Pattern UUID_FORM =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private InvoiceIds() {
    }

    /** Empty when the text is not a UUID in its canonical form. */
    static Optional<UUID> parse(String invoiceId) {
        return invoiceId != null && UUID_FORM.matcher(invoiceId).matches()
                ? Optional.of(UUID.fromString(invoiceId))
                : Optional.empty();
    }
}
