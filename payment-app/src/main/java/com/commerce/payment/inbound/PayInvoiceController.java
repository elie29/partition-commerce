package com.commerce.payment.inbound;

import static com.commerce.payment.inbound.PaymentModels.toInvoice;

import com.commerce.payment.application.PayInvoiceUseCase;
import com.commerce.payment.inbound.api.PaymentApi;
import com.commerce.payment.inbound.api.model.Invoice;
import com.commerce.payment.inbound.api.model.PayInvoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** The operations of the tag {@code Payment} of `docs/contracts/billing.openapi.json`. */
@RestController
public class PayInvoiceController implements PaymentApi {

    private final PayInvoiceUseCase payInvoice;

    public PayInvoiceController(PayInvoiceUseCase payInvoice) {
        this.payInvoice = payInvoice;
    }

    /** The request model prints its card token: it is handed to the use case and never logged (BR-24). */
    @Override
    public ResponseEntity<Invoice> payInvoice(String invoiceId, PayInvoice request) {
        return ResponseEntity.ok(toInvoice(payInvoice.pay(invoiceId, request.getCardToken())));
    }
}
