package com.commerce.payment.application;

import com.commerce.payment.domain.CardToken;
import com.commerce.payment.domain.InvalidPayment;
import com.commerce.payment.domain.Money;
import com.commerce.payment.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The customer pays an issued invoice by card, through CardPay (REQ-4); anyone holding its identifier may (BR-21).
 * The card token goes to CardPay and nowhere else (BR-24).
 */
@Service
public class PayInvoiceUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(PayInvoiceUseCase.class);
    private final PayableInvoices invoices;
    private final FraudReview fraudReview;
    private final CardPay cardPay;
    private final RecordPayment recordPayment;

    public PayInvoiceUseCase(
            PayableInvoices invoices, FraudReview fraudReview, CardPay cardPay, RecordPayment recordPayment) {
        this.invoices = invoices;
        this.fraudReview = fraudReview;
        this.cardPay = cardPay;
        this.recordPayment = recordPayment;
    }

    /**
     * Checks the content, then the invoice, then the fraud review, then the state; charges the whole amount once and
     * records the payment (BR-5, BR-22, AR-3).
     *
     * @throws PaymentContentRefused when the card token is not bounded by AR-1, before the invoice is read (BR-22)
     * @throws InvoiceNotFound when the identifier names no invoice, or is not a UUID (BR-21)
     * @throws PayForbidden when the invoice's customer is under fraud review, before its state is checked (AR-3)
     * @throws PayRefused when the invoice is paid or cancelled, before CardPay is called (BR-20), or when a
     *     simultaneous pay or cancel was recorded first (BR-5): the charge is then kept nowhere
     * @throws CardDeclined when CardPay declines the card; nothing is recorded (BR-5)
     * @throws InvalidPayment when CardPay answers a charge reference not bounded by AR-1: a defect (BR-5)
     */
    public InvoiceToPay pay(String invoiceId, String cardToken) {
        CardToken token;
        try {
            token = new CardToken(cardToken);
        } catch (InvalidPayment e) {
            throw new PaymentContentRefused(e.getMessage());
        }
        InvoiceToPay invoice = InvoiceIds.parse(invoiceId)
                .flatMap(invoices::find)
                .orElseThrow(InvoiceNotFound::new);
        if (invoices.customerOf(invoice.invoiceId()).filter(fraudReview::underReview).isPresent()) {
            throw new PayForbidden();
        }
        if (!invoice.issued()) {
            throw new PayRefused("BR-20: only an invoice whose status is ISSUED is paid", null);
        }
        Money amount = new Money(invoice.amountMinor(), invoice.currency());
        Payment payment = switch (cardPay.charge(token, amount)) {
            case Charge.Declined declined -> throw new CardDeclined();
            case Charge.Accepted accepted -> Payment.of(invoice.invoiceId(), amount, accepted.chargeReference());
        };
        InvoiceToPay paid;
        try {
            paid = recordPayment.record(payment);
        } catch (InvoiceNoLongerIssued e) {
            throw new PayRefused("BR-5: a simultaneous pay or cancel was recorded first", e);
        }
        LOG.info("Invoice {} paid for order {}", paid.invoiceId(), paid.orderId());
        return paid;
    }
}
