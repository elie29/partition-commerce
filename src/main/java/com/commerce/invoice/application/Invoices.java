package com.commerce.invoice.application;

import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.InvoiceStatus;
import java.util.Optional;
import java.util.UUID;

/** The port to the invoices recorded; an outbound adapter implements it. */
public interface Invoices {

    /**
     * Records an issued invoice with its lines, all or nothing.
     *
     * @throws OrderAlreadyInvoiced when the order already has an invoice that is not cancelled, a simultaneous issue included (BR-1)
     */
    void add(Invoice invoice);

    Optional<Invoice> find(UUID id);

    /**
     * Records the status of an invoice, and nothing else, if its recorded status is still {@code from}.
     *
     * @return false when the recorded status is no longer {@code from}: a simultaneous change came first
     */
    boolean changeStatus(UUID id, InvoiceStatus from, InvoiceStatus to);

    /**
     * Records the payment of a paid invoice — its status, its charge reference and the time of the payment, the moment
     * the record commits — and nothing else, if its recorded status is still {@code ISSUED} (BR-3, BR-5).
     *
     * @return false when the recorded status is no longer {@code ISSUED}: a simultaneous pay or cancel came first
     */
    boolean recordPayment(Invoice paid);
}
