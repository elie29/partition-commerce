package com.commerce.payment.outbound;

/**
 * {@code CustomerStatus} did not answer: unreachable, slower than 2 seconds, or a failure (BR-32), 500. It names the
 * kind of failure only, never the customer asked about, which the client's own exception carries in its URL, so that
 * no log or error names a customer under fraud review (BR-31, BR-32).
 */
class CustomerStatusUnavailable extends RuntimeException {

    CustomerStatusUnavailable(Exception failure) {
        super("BR-32: CustomerStatus did not answer (" + failure.getClass().getSimpleName() + ")", null, false, false);
    }
}
