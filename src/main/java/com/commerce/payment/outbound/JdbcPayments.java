package com.commerce.payment.outbound;

import com.commerce.payment.application.Payments;
import com.commerce.payment.domain.Payment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** The payments in the database, through {@link JdbcClient} (V4); no foreign key to {@code invoice}'s tables. */
@Repository
class JdbcPayments implements Payments {

    private final JdbcClient jdbc;

    JdbcPayments(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** The time of the payment is the database's, taken by the last statement of the caller's transaction (BR-24). */
    @Override
    @Transactional
    public void add(Payment payment) {
        jdbc.sql("INSERT INTO payment (payment_id, invoice_id, amount_minor, amount_currency, charge_reference, paid_at)"
                        + " VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)")
                .params(payment.id(), payment.invoiceId(), payment.amount().amountMinor(),
                        payment.amount().currency(), payment.chargeReference())
                .update();
    }
}
