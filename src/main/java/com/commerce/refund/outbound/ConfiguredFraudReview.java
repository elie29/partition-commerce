package com.commerce.refund.outbound;

import com.commerce.refund.application.FraudReview;
import com.commerce.refund.domain.CustomerId;
import com.commerce.refund.domain.InvalidRefund;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * The port {@link FraudReview} over the list {@code commerce.fraud-review.customers} of {@code application.yaml}, read
 * once, at start-up: an edit counts at the next restart (AR-3; TL, 2026-10-19). A missing list, or an entry AR-1
 * refuses, stops the start, with a message that names no entry.
 */
@Component("refundFraudReview")
class ConfiguredFraudReview implements FraudReview {

    static final String KEY = "commerce.fraud-review.customers";

    private final Set<String> customers;

    ConfiguredFraudReview(Environment environment) {
        List<String> entries = Binder.get(environment)
                .bind(KEY, Bindable.listOf(String.class))
                .orElseThrow(() -> new IllegalStateException("AR-3: " + KEY + " is missing"));
        try {
            this.customers = entries.stream().map(CustomerId::new).map(CustomerId::value).collect(Collectors.toSet());
        } catch (InvalidRefund e) {
            throw new IllegalStateException("AR-3: an entry of " + KEY + " is blank or longer than 255 characters");
        }
    }

    @Override
    public boolean underReview(String customerId) {
        return customers.contains(customerId);
    }
}
