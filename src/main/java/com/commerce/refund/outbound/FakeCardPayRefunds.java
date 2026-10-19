package com.commerce.refund.outbound;

import com.commerce.refund.application.CardPayRefunds;
import com.commerce.refund.domain.Money;
import org.springframework.stereotype.Component;

/**
 * CardPay's only adapter for refunds while CardPay is not reachable: it sends every refund, wherever the service runs;
 * no URL, no credentials (TL, 2026-10-19). A real adapter replaces it when CardPay is reachable.
 */
@Component
class FakeCardPayRefunds implements CardPayRefunds {

    @Override
    public void send(String chargeReference, Money amount) {
        // Sent: the fake accepts every refund.
    }
}
