package com.commerce.invoice.domain;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * What the customer owes for one confirmed order, line by line. Issued, it is never modified (BR-3): the class has no
 * setter and its lines are an unmodifiable copy.
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
        this.orderId = Text.require(orderId, "BR-11: an order identifier is non-blank, at most 255 characters");
        this.customerId = Text.require(customerId, "BR-11: a customer identifier is non-blank, at most 255 characters");
        this.status = Objects.requireNonNull(status);
        if (lines == null || lines.isEmpty() || lines.contains(null)) {
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
