package com.commerce.fraud.inbound;

import com.commerce.fraud.application.CustomerStatus;
import com.commerce.fraud.inbound.api.CustomerStatusApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * The contract {@link CustomerStatus} over HTTP, the tag {@code CustomerStatus} of
 * `docs/contracts/billing-internal.openapi.json` (GoF Adapter): is this customer, matched as the same text, blocked
 * (BR-4). A failure of the store reaches the framework, 500.
 */
@RestController
public class CustomerStatusController implements CustomerStatusApi {

    private final CustomerStatus customerStatus;

    public CustomerStatusController(CustomerStatus customerStatus) {
        this.customerStatus = customerStatus;
    }

    @Override
    public ResponseEntity<com.commerce.fraud.inbound.api.model.CustomerStatus> getCustomerStatus(String customerId) {
        return ResponseEntity.ok(new com.commerce.fraud.inbound.api.model.CustomerStatus(
                customerStatus.blocked(customerId)));
    }
}
