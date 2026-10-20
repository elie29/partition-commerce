package com.commerce.invoice.inbound;

import com.commerce.invoice.application.GetInvoiceUseCase;
import com.commerce.invoice.application.InvoiceNotFound;
import com.commerce.invoice.application.InvoicePayments;
import com.commerce.invoice.application.InvoiceView;
import com.commerce.invoice.inbound.internal.api.InvoicePaymentsApi;
import com.commerce.invoice.inbound.internal.api.model.Invoice;
import com.commerce.invoice.inbound.internal.api.model.InvoiceCustomer;
import com.commerce.invoice.inbound.internal.api.model.Line;
import com.commerce.invoice.inbound.internal.api.model.Money;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * The port {@link InvoicePayments} over HTTP, the tag {@code InvoicePayments} of
 * `docs/contracts/billing-internal.openapi.json` (GoF Adapter): what {@code payment-app} asks {@code invoice}. The
 * identifier of the path is read first, as {@code getInvoice} reads it: not a UUID, or no invoice, 404 (architect,
 * 2026-10-20). {@code recordPaymentCancelled} keeps the generated answer, 501, until REQ-5 builds it.
 */
@RestController
public class InvoicePaymentsController implements InvoicePaymentsApi {

    private final GetInvoiceUseCase getInvoice;
    private final InvoicePayments invoicePayments;

    public InvoicePaymentsController(GetInvoiceUseCase getInvoice, InvoicePayments invoicePayments) {
        this.getInvoice = getInvoice;
        this.invoicePayments = invoicePayments;
    }

    @Override
    public ResponseEntity<Invoice> getInvoiceForPayment(String invoiceId) {
        return ResponseEntity.ok(toInvoice(getInvoice.read(invoiceId)));
    }

    @Override
    public ResponseEntity<InvoiceCustomer> getInvoiceCustomer(String invoiceId) {
        String customerId = invoicePayments.customerOf(getInvoice.read(invoiceId).invoiceId())
                .orElseThrow(InvoiceNotFound::new);
        return ResponseEntity.ok(new InvoiceCustomer(customerId));
    }

    /** BR-23: refused, 409, when the invoice is no longer {@code ISSUED}. */
    @Override
    public ResponseEntity<Invoice> recordPayment(String invoiceId) {
        return ResponseEntity.ok(toInvoice(invoicePayments.recordPayment(getInvoice.read(invoiceId).invoiceId())));
    }

    private static Invoice toInvoice(InvoiceView view) {
        return new Invoice()
                .invoiceId(view.invoiceId().toString())
                .orderId(view.orderId())
                .status(Invoice.StatusEnum.fromValue(view.status()))
                .amount(new Money(view.amountMinor(), view.currency()))
                .lines(view.lines().stream()
                        .map(l -> new Line(l.label(), new Money(l.priceMinor(), l.currency())))
                        .toList());
    }
}
