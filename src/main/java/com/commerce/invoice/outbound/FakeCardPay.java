package com.commerce.invoice.outbound;

import com.commerce.invoice.application.CardPay;
import com.commerce.invoice.application.Charge;
import com.commerce.invoice.domain.CardToken;
import com.commerce.invoice.domain.Money;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * CardPay's only adapter while CardPay is not reachable: it accepts every card, wherever the service runs, since the
 * demo has no real customer; no URL, no credentials. A real adapter replaces it when CardPay is reachable.
 */
@Component
class FakeCardPay implements CardPay {

    @Override
    public Charge charge(CardToken cardToken, Money amount) {
        return new Charge.Accepted("fake-" + UUID.randomUUID());
    }
}
