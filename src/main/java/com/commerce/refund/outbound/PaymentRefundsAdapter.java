package com.commerce.refund.outbound;

import com.commerce.payment.application.PaidPayment;
import com.commerce.payment.application.PaymentRefunds;
import com.commerce.payment.application.RefundableInvoice;
import com.commerce.refund.application.InvoiceToRefund;
import com.commerce.refund.application.RefundableInvoices;
import com.commerce.refund.domain.Money;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * The port {@link RefundableInvoices} over {@code payment}'s contract {@link PaymentRefunds}, in process (GoF Adapter):
 * the only class of {@code refund} that knows {@code payment}, and only through that contract.
 */
@Component
class PaymentRefundsAdapter implements RefundableInvoices {

    private final PaymentRefunds paymentRefunds;

    PaymentRefundsAdapter(PaymentRefunds paymentRefunds) {
        this.paymentRefunds = paymentRefunds;
    }

    @Override
    public Optional<InvoiceToRefund> find(UUID invoiceId) {
        return paymentRefunds.find(invoiceId).map(PaymentRefundsAdapter::toRefund);
    }

    private static InvoiceToRefund toRefund(RefundableInvoice invoice) {
        return new InvoiceToRefund(invoice.invoiceId(), invoice.customerId(),
                invoice.payment().map(PaymentRefundsAdapter::toPaid));
    }

    private static InvoiceToRefund.Paid toPaid(PaidPayment payment) {
        return new InvoiceToRefund.Paid(
                new Money(payment.amountMinor(), payment.currency()), payment.chargeReference());
    }
}
