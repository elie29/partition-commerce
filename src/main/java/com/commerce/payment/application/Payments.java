package com.commerce.payment.application;

import com.commerce.payment.domain.Payment;

/** The port to the payments recorded; an outbound adapter implements it. */
public interface Payments {

    /** Records a payment, its time being the moment the statement runs, in the caller's transaction (BR-24). */
    void add(Payment payment);
}
