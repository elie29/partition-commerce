package com.commerce.invoice.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * What the customer owes for one confirmed order, line by line. Issued, it is never modified (BR-3): the class has no
 * setter and its lines are an unmodifiable copy; a change of status returns a new instance (P-1).
 */
public final class Invoice {

    private final UUID id;
    private final String orderId;
    private final String customerId;
    private final InvoiceStatus status;
    private final List<Line> lines;
    private final Money amount;

    private Invoice(UUID id, String orderId, String customerId, InvoiceStatus status, List<Line> lines) {
        this.id = Objects.requireNonNull(id);
        this.orderId = Text.require(orderId, "BR-11: an order identifier is bounded as a label is (BR-12)");
        this.customerId = Text.require(customerId, "BR-11: a customer identifier is bounded as a label is (BR-12)");
        this.status = Objects.requireNonNull(status);
        if (lines == null || lines.isEmpty() || lines.stream().anyMatch(Objects::isNull)) {
            throw new InvalidInvoice("BR-16: an invoice has at least one line");
        }
        this.lines = List.copyOf(lines);
        this.amount = this.lines.stream().map(Line::price).reduce(Money::plus).orElseThrow();
    }

    /** Issues the invoice of an order, with an identifier of its own (BR-14). */
    public static Invoice issue(String orderId, String customerId, List<Line> lines) {
        return new Invoice(UUID.randomUUID(), orderId, customerId, InvoiceStatus.ISSUED, lines);
    }

    /** An invoice already issued, as it was recorded. */
    public static Invoice restore(UUID id, String orderId, String customerId, InvoiceStatus status, List<Line> lines) {
        return new Invoice(id, orderId, customerId, status, lines);
    }

    /**
     * Cancels the invoice before any payment, at no fee (BR-4): the same invoice with its status changed and nothing
     * else (BR-3).
     *
     * @throws InvoiceNotCancellable when the invoice is paid (BR-4) or already cancelled (BR-18)
     */
    public Invoice cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new InvoiceNotCancellable("BR-4: an invoice is cancelled only before any payment");
        }
        if (status != InvoiceStatus.ISSUED) {
            throw new InvoiceNotCancellable("BR-18: a cancelled invoice is not cancelled again");
        }
        return new Invoice(id, orderId, customerId, InvoiceStatus.CANCELLED, lines);
    }

    /**
     * Records the invoice paid, for {@code payment} (BR-23): the same invoice with its status changed and nothing else
     * (BR-3).
     *
     * @throws InvoiceNotPayable when the invoice is no longer {@code ISSUED}
     */
    public Invoice recordPayment() {
        if (status != InvoiceStatus.ISSUED) {
            throw new InvoiceNotPayable("BR-23: a payment is recorded only on an invoice still ISSUED");
        }
        return new Invoice(id, orderId, customerId, InvoiceStatus.PAID, lines);
    }

    public UUID id() {
        return id;
    }

    public String orderId() {
        return orderId;
    }

    public String customerId() {
        return customerId;
    }

    public InvoiceStatus status() {
        return status;
    }

    /** The lines, in the order they were issued (BR-17). */
    public List<Line> lines() {
        return lines;
    }

    /** The sum of the prices of the lines, in their currency (BR-2). */
    public Money amount() {
        return amount;
    }
}
