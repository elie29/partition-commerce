package com.commerce.refund.application;

import com.commerce.refund.domain.InvalidRefund;
import com.commerce.refund.domain.Money;
import com.commerce.refund.domain.Refund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The accountant refunds all or part of a paid invoice, to the card, through CardPay (REQ-6); anyone holding its
 * identifier may, in this version (PO). A log names the refund's identifiers and its status only (BR-31).
 */
@Service
public class RefundInvoiceUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(RefundInvoiceUseCase.class);
    private final RefundableInvoices invoices;
    private final FraudReview fraudReview;
    private final Refunds refunds;
    private final CardPayRefunds cardPay;

    public RefundInvoiceUseCase(
            RefundableInvoices invoices, FraudReview fraudReview, Refunds refunds, CardPayRefunds cardPay) {
        this.invoices = invoices;
        this.fraudReview = fraudReview;
        this.refunds = refunds;
        this.cardPay = cardPay;
    }

    /**
     * Checks the content, then the invoice, then the fraud review, then the payment; records the refund
     * {@code REQUESTED} under the cap, sends it, and records it {@code SENT} (BR-25 to BR-29, AR-3).
     *
     * @throws RefundContentRefused when the amount is not positive (BR-26), before the invoice is read, or when its
     *     currency is not the amount paid's (BR-25)
     * @throws InvoiceNotFound when the identifier names no invoice, or is not a UUID (BR-27)
     * @throws RefundForbidden when the invoice's customer is under fraud review; nothing is recorded (AR-3)
     * @throws RefundRefused when the invoice is not paid, a cancelled payment included (BR-25, BR-29), or when the
     *     refund exceeds what remains, a simultaneous refund recorded first included (BR-25); nothing is recorded
     */
    public RefundView refund(String invoiceId, long amountMinor, String currency) {
        Money amount;
        try {
            amount = Money.ofRefund(amountMinor, currency);
        } catch (InvalidRefund e) {
            throw new RefundContentRefused(e.getMessage());
        }
        InvoiceToRefund invoice = InvoiceIds.parse(invoiceId)
                .flatMap(invoices::find)
                .orElseThrow(InvoiceNotFound::new);
        if (fraudReview.underReview(invoice.customerId())) {
            throw new RefundForbidden();
        }
        InvoiceToRefund.Paid paid = invoice.payment()
                .orElseThrow(() -> new RefundRefused("BR-25: only a paid invoice is refunded"));
        if (!amount.currency().equals(paid.amountPaid().currency())) {
            throw new RefundContentRefused("BR-25: a refund is in the currency of the amount paid");
        }
        Refund requested = Refund.request(invoice.invoiceId(), amount, paid.amountPaid(), paid.chargeReference());
        refunds.openCap(invoice.invoiceId(), paid.amountPaid());
        if (!refunds.addUnderCap(requested)) {
            throw new RefundRefused("BR-25: the refunds of an invoice never exceed its amount paid");
        }
        cardPay.send(requested.chargeReference(), requested.amount());
        Refund sent = requested.sent();
        refunds.recordSent(sent);
        LOG.info("Refund {} of invoice {} {}", sent.id(), sent.invoiceId(), sent.status());
        return RefundView.of(sent);
    }
}
