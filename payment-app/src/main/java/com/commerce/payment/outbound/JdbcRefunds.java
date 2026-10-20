package com.commerce.payment.outbound;

import com.commerce.payment.application.Refunds;
import com.commerce.payment.domain.Money;
import com.commerce.payment.domain.Refund;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The refunds and the cap of each invoice in the database, through {@link JdbcClient} (V6); no foreign key to another
 * context's tables. The cap holds by one conditional update of a running total (BR-25, P-3).
 */
@Repository
class JdbcRefunds implements Refunds {

    private final JdbcClient jdbc;

    JdbcRefunds(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Of two first refunds of one invoice at the same moment, the second finds the row the first opened. */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void openCap(UUID invoiceId, Money amountPaid) {
        try {
            jdbc.sql("INSERT INTO refund_cap (invoice_id, amount_paid_minor, amount_paid_currency, refunded_minor,"
                            + " refunded_currency) SELECT ?, ?, ?, 0, ?"
                            + " WHERE NOT EXISTS (SELECT 1 FROM refund_cap WHERE invoice_id = ?)")
                    .params(invoiceId, amountPaid.amountMinor(), amountPaid.currency(), amountPaid.currency(),
                            invoiceId)
                    .update();
        } catch (DuplicateKeyException e) {
            // Opened by a simultaneous refund of the same invoice.
        }
    }

    /**
     * The running total grows only while it stays within the amount paid; a simultaneous update of the same row waits
     * for the first to commit and is checked against the total it left.
     */
    @Override
    @Transactional
    public boolean addUnderCap(Refund refund) {
        int counted = jdbc.sql("UPDATE refund_cap SET refunded_minor = refunded_minor + ?"
                        + " WHERE invoice_id = ? AND refunded_currency = ? AND refunded_minor + ? <= amount_paid_minor")
                .params(refund.amount().amountMinor(), refund.invoiceId(), refund.amount().currency(),
                        refund.amount().amountMinor())
                .update();
        if (counted == 0) {
            return false;
        }
        jdbc.sql("INSERT INTO refund (refund_id, invoice_id, amount_minor, amount_currency, charge_reference, status,"
                        + " requested_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)")
                .params(refund.id(), refund.invoiceId(), refund.amount().amountMinor(), refund.amount().currency(),
                        refund.chargeReference(), refund.status().name())
                .update();
        return true;
    }

    @Override
    @Transactional
    public void recordSent(Refund refund) {
        jdbc.sql("UPDATE refund SET status = ? WHERE refund_id = ? AND status = 'REQUESTED'")
                .params(refund.status().name(), refund.id())
                .update();
    }
}
