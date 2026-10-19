package com.commerce.invoice.application;

import com.commerce.invoice.domain.CardToken;
import com.commerce.invoice.domain.InvalidInvoice;
import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.InvoiceNotPayable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The customer pays an issued invoice by card, through CardPay (REQ-4); anyone holding its identifier may (BR-21).
 * The card token goes to CardPay and nowhere else (BR-3).
 */
@Service
public class PayInvoiceUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(PayInvoiceUseCase.class);

    private final Invoices invoices;
    private final CardPay cardPay;

    public PayInvoiceUseCase(Invoices invoices, CardPay cardPay) {
        this.invoices = invoices;
        this.cardPay = cardPay;
    }

    /**
     * Checks the content, then the invoice, then charges the whole amount once and records the payment (BR-5, BR-22).
     *
     * @throws InvoiceContentRefused when the card token is not bounded as a label is, before the invoice is read (BR-22)
     * @throws InvoiceNotFound when the identifier names no invoice, or is not a UUID (BR-21)
     * @throws PayRefused when the invoice is paid or cancelled, before CardPay is called (BR-20), or when a
     *     simultaneous pay or cancel was recorded first (BR-5): the charge is then kept nowhere
     * @throws CardDeclined when CardPay declines the card; nothing is recorded (BR-5)
     */
    public InvoiceView pay(String invoiceId, String cardToken) {
        CardToken token;
        try {
            token = new CardToken(cardToken);
        } catch (InvalidInvoice e) {
            throw new InvoiceContentRefused(e.getMessage(), null);
        }
        Invoice invoice = InvoiceIds.parse(invoiceId)
                .flatMap(invoices::find)
                .orElseThrow(InvoiceNotFound::new);
        try {
            invoice.requirePayable();
        } catch (InvoiceNotPayable e) {
            throw new PayRefused(e.getMessage(), e);
        }
        Invoice paid = switch (cardPay.charge(token, invoice.amount())) {
            case Charge.Declined declined -> throw new CardDeclined();
            case Charge.Accepted accepted -> invoice.pay(accepted.chargeReference());
        };
        if (!invoices.recordPayment(paid)) {
            throw new PayRefused("BR-5: a simultaneous pay or cancel was recorded first", null);
        }
        LOG.info("Invoice {} paid for order {}", paid.id(), paid.orderId());
        return InvoiceView.of(paid);
    }
}
