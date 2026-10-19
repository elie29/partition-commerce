package com.commerce.invoice.inbound;

import static com.commerce.invoice.inbound.InvoiceModels.toInvoice;

import com.commerce.invoice.application.PayInvoiceUseCase;
import com.commerce.invoice.inbound.api.PaymentApi;
import com.commerce.invoice.inbound.api.model.Invoice;
import com.commerce.invoice.inbound.api.model.PayInvoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** The operations of the tag {@code Payment} of `docs/contracts/billing.openapi.json`. */
@RestController
public class PayInvoiceController implements PaymentApi {

    private final PayInvoiceUseCase payInvoice;

    public PayInvoiceController(PayInvoiceUseCase payInvoice) {
        this.payInvoice = payInvoice;
    }

    /** The request model prints its card token: it is handed to the use case and never logged (BR-3). */
    @Override
    public ResponseEntity<Invoice> payInvoice(String invoiceId, PayInvoice request) {
        return ResponseEntity.ok(toInvoice(payInvoice.pay(invoiceId, request.getCardToken())));
    }
}
