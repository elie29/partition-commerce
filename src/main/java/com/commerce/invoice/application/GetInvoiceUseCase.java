package com.commerce.invoice.application;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Anyone holding the identifier of an invoice reads it back (BR-15). */
@Service
public class GetInvoiceUseCase {

    /** The canonical form only: {@link UUID#fromString} would also take {@code 1-1-1-1-1}. */
    private static final Pattern UUID_FORM =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private final Invoices invoices;

    public GetInvoiceUseCase(Invoices invoices) {
        this.invoices = invoices;
    }

    /** @throws InvoiceNotFound when the identifier names no invoice, or is not a UUID */
    public InvoiceView read(String invoiceId) {
        return parse(invoiceId)
                .flatMap(invoices::find)
                .map(InvoiceView::of)
                .orElseThrow(InvoiceNotFound::new);
    }

    private static Optional<UUID> parse(String invoiceId) {
        return invoiceId != null && UUID_FORM.matcher(invoiceId).matches()
                ? Optional.of(UUID.fromString(invoiceId))
                : Optional.empty();
    }
}
