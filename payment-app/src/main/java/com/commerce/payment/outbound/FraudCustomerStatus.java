package com.commerce.payment.outbound;

import com.commerce.payment.application.FraudReview;
import com.commerce.payment.outbound.internal.api.CustomerStatusApi;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * The port {@link FraudReview} over {@code fraud}'s {@code getCustomerStatus} of
 * `docs/contracts/billing-internal.openapi.json`, over HTTP (BR-32): the one class of {@code payment} that names
 * {@code fraud}. A failure, 2 seconds without an answer included, reaches the use case as
 * {@link CustomerStatusUnavailable}, which does not name the customer, and the framework answers 500 before CardPay is
 * called and before any record.
 */
@Component("paymentFraudReview")
class FraudCustomerStatus implements FraudReview {

    private final CustomerStatusApi customerStatus;

    FraudCustomerStatus(CustomerStatusApi customerStatus) {
        this.customerStatus = customerStatus;
    }

    @Override
    public boolean underReview(String customerId) {
        try {
            return Boolean.TRUE.equals(customerStatus.getCustomerStatus(customerId).getBody().getBlocked());
        } catch (RestClientException e) {
            throw new CustomerStatusUnavailable(e);
        }
    }
}
