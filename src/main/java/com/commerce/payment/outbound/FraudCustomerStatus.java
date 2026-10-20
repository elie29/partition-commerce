package com.commerce.payment.outbound;

import com.commerce.fraud.application.CustomerStatus;
import com.commerce.payment.application.FraudReview;
import org.springframework.stereotype.Component;

/**
 * The port {@link FraudReview} over {@code fraud}'s contract {@link CustomerStatus}, in process (BR-32; architect,
 * 2026-10-20): the one class of {@code payment} that names {@code fraud}. A failure of {@code fraud} reaches the use
 * case as it is thrown, and the framework answers 500 before CardPay is called and before any record.
 */
@Component("paymentFraudReview")
class FraudCustomerStatus implements FraudReview {

    private final CustomerStatus customerStatus;

    FraudCustomerStatus(CustomerStatus customerStatus) {
        this.customerStatus = customerStatus;
    }

    @Override
    public boolean underReview(String customerId) {
        return customerStatus.blocked(customerId);
    }
}
