package com.commerce.product.inbound;

/** A body that is not JSON, or not a JSON object: one item, no field (AR-1). Carries no value sent. */
class BodyUnreadable extends RuntimeException {

    BodyUnreadable() {
        super("AR-1: the body cannot be read");
    }
}
