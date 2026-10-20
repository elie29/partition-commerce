package com.commerce.payment.application;

import com.commerce.payment.domain.Payment;
import org.springframework.stereotype.Service;

/**
 * The two writes of a pay (BR-5): the invoice recorded paid by {@code invoice}, which decides, then the payment,
 * stored only once {@code invoice} answered it recorded, so that its time is taken last (BR-24). Of a pay and a cancel
 * at the same moment, the first {@code invoice} records holds. A payment {@code invoice} recorded and this store then
 * failed to keep stays open until REQ-5 (architect, 2026-10-20).
 */
@Service
public class RecordPayment {

    private final PayableInvoices invoices;
    private final Payments payments;

    public RecordPayment(PayableInvoices invoices, Payments payments) {
        this.invoices = invoices;
        this.payments = payments;
    }

    /** @throws InvoiceNoLongerIssued when the invoice is no longer {@code ISSUED}; nothing is stored */
    public InvoiceToPay record(Payment payment) {
        InvoiceToPay paid = invoices.recordPaid(payment.invoiceId());
        payments.add(payment);
        return paid;
    }
}
