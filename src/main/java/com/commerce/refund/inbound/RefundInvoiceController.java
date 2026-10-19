package com.commerce.refund.inbound;

import com.commerce.refund.application.RefundInvoiceUseCase;
import com.commerce.refund.application.RefundView;
import com.commerce.refund.inbound.api.RefundApi;
import com.commerce.refund.inbound.api.model.Money;
import com.commerce.refund.inbound.api.model.Refund;
import com.commerce.refund.inbound.api.model.RefundInvoice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** The operations of the tag {@code Refund} of `docs/contracts/billing.openapi.json`. */
@RestController
public class RefundInvoiceController implements RefundApi {

    private final RefundInvoiceUseCase refundInvoice;

    public RefundInvoiceController(RefundInvoiceUseCase refundInvoice) {
        this.refundInvoice = refundInvoice;
    }

    /** The request model prints its amount: it is handed to the use case and never logged (BR-31). */
    @Override
    public ResponseEntity<Refund> refundInvoice(String invoiceId, RefundInvoice request) {
        Money amount = request.getAmount();
        RefundView sent = refundInvoice.refund(invoiceId, amount.getAmountMinor(), amount.getCurrency());
        return ResponseEntity.status(HttpStatus.CREATED).body(toRefund(sent));
    }

    private static Refund toRefund(RefundView refund) {
        return new Refund()
                .refundId(refund.refundId().toString())
                .invoiceId(refund.invoiceId().toString())
                .amount(new Money(refund.amountMinor(), refund.currency()))
                .status(Refund.StatusEnum.fromValue(refund.status()));
    }
}
