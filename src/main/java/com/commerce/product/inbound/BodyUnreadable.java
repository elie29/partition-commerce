package com.commerce.product.inbound;

/** A body that is not JSON, or not a JSON object: one item, no field (BR-27). Carries no value sent. */
class BodyUnreadable extends RuntimeException {

    BodyUnreadable() {
        super("BR-27: the body cannot be read");
    }
}
