package com.commerce.fraud.application;

import org.springframework.stereotype.Service;

/** Answers {@link CustomerStatus} from the open reviews of the store (BR-4). */
@Service
class CustomerStatusUseCase implements CustomerStatus {

    private final Reviews reviews;

    CustomerStatusUseCase(Reviews reviews) {
        this.reviews = reviews;
    }

    @Override
    public boolean blocked(String customerId) {
        return reviews.findOpen(customerId).isPresent();
    }
}
