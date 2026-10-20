package com.commerce.payment.application;

import com.commerce.payment.domain.Money;
import com.commerce.payment.domain.Refund;
import java.util.UUID;

/** The port to the refunds recorded and to the cap of each invoice (BR-25, P-3); an outbound adapter implements it. */
public interface Refunds {

    /** Opens the cap of the invoice at its amount paid, in a transaction of its own; nothing when it is open already. */
    void openCap(UUID invoiceId, Money amountPaid);

    /**
     * Records a requested refund under the cap, in one transaction: of two refunds of one invoice at the same moment,
     * the first to commit counts, and the other is checked against what then remains.
     *
     * @return false when the refund exceeds what remains of the amount paid; nothing is recorded
     */
    boolean addUnderCap(Refund refund);

    /** Records the refund sent (BR-28). */
    void recordSent(Refund refund);
}
