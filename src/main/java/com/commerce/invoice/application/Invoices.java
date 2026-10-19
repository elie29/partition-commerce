package com.commerce.invoice.application;

import com.commerce.invoice.domain.Invoice;
import java.util.Optional;
import java.util.UUID;

/** The port to the invoices recorded; an outbound adapter implements it. */
public interface Invoices {

    /**
     * Records an issued invoice with its lines, all or nothing.
     *
     * @throws OrderAlreadyInvoiced when the order already has an invoice, a simultaneous issue included (BR-1)
     */
    void add(Invoice invoice);

    Optional<Invoice> find(UUID id);
}
