package com.commerce.fraud.domain;

/** Open: placed, not lifted. Lifted: ended by the risk team, final; a place after a lift is a new review (BR-5, P-7). */
public enum ReviewStatus {
    OPEN,
    LIFTED
}
