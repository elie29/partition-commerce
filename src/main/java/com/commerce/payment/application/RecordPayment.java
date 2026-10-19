package com.commerce.payment.application;

import com.commerce.payment.domain.Payment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The two writes of a pay in one transaction (BR-5): the invoice recorded paid by {@code invoice}, then the payment, last,
 * so that its time is taken just before the commit (BR-24). Of a pay and a cancel at the same moment, the first to commit
 * holds both writes.
 */
@Service
public class RecordPayment {

    private final PayableInvoices invoices;
    private final Payments payments;

    public RecordPayment(PayableInvoices invoices, Payments payments) {
        this.invoices = invoices;
        this.payments = payments;
    }

    /** @throws InvoiceNoLongerIssued when the invoice is no longer {@code ISSUED}; nothing is recorded */
    @Transactional
    public InvoiceToPay record(Payment payment) {
        InvoiceToPay paid = invoices.recordPaid(payment.invoiceId());
        payments.add(payment);
        return paid;
    }
}
