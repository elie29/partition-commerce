package com.commerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.commerce.product.domain.Channel;
import com.commerce.product.domain.Currency;
import com.commerce.product.domain.InvalidPriceList;
import com.commerce.product.domain.Money;
import com.commerce.product.domain.Price;
import com.commerce.product.domain.PriceList;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The domain of {@code price-list}, without Spring or a database: BR-2 to BR-5 and Applies of the area. */
class PriceListTest {

    private static final UUID PRODUCT = UUID.randomUUID();

    @Test
    void a_currency_is_a_known_iso_4217_code_of_the_jdk_list_that_does_not_start_with_x() {
        assertThat(Currency.isKnown("EUR")).isTrue();
        assertThat(Currency.isKnown("DEM")).as("withdrawn, still listed").isTrue();
        assertThat(Currency.isKnown("XYZ")).isFalse();
        assertThat(Currency.isKnown("XAU")).as("listed, starts with X").isFalse();
        assertThat(Currency.isKnown("eur")).isFalse();
        assertThatThrownBy(() -> Currency.of("XYZ")).isInstanceOf(InvalidPriceList.class);
    }

    @Test
    void a_stored_code_is_read_as_kept_even_one_the_jdk_does_not_list() {
        assertThatThrownBy(() -> Currency.of("ZZZ")).isInstanceOf(InvalidPriceList.class);

        assertThat(Currency.stored("ZZZ").code()).isEqualTo("ZZZ");
        assertThat(Currency.stored("ZZZ")).isEqualTo(Currency.stored("ZZZ"));
        assertThat(Currency.stored("EUR")).isEqualTo(Currency.of("EUR"));
    }

    @Test
    void the_amount_of_a_price_is_never_negative_and_zero_is_a_price() {
        assertThat(Money.isAmount(0)).isTrue();
        assertThat(Money.isAmount(-1)).isFalse();
        assertThatThrownBy(() -> new Money(-1, Currency.of("EUR"))).isInstanceOf(InvalidPriceList.class);
    }

    @Test
    void a_name_is_not_blank_non_breaking_spaces_included() {
        assertThat(PriceList.isBlankName("  ")).isTrue();
        assertThat(PriceList.isBlankName("Shop")).isFalse();
        assertThatThrownBy(() -> new PriceList(UUID.randomUUID(), " ", Currency.of("EUR"), Channel.SHOP))
                .isInstanceOf(InvalidPriceList.class);
    }

    @Test
    void a_price_is_in_its_price_list_s_currency() {
        PriceList list = new PriceList(UUID.randomUUID(), "Shop", Currency.of("EUR"), Channel.SHOP);
        Instant now = Instant.parse("2026-10-19T10:00:00Z");

        Price price = list.price(PRODUCT, new Money(0, Currency.of("EUR")), now);

        assertThat(price.priceListId()).isEqualTo(list.id());
        assertThat(price.productId()).isEqualTo(PRODUCT);
        assertThat(list.isInItsCurrency(Currency.of("USD"))).isFalse();
        assertThatThrownBy(() -> list.price(PRODUCT, new Money(100, Currency.of("USD")), now))
                .isInstanceOf(InvalidPriceList.class);
    }

    @Test
    void a_price_applies_on_a_day_when_its_effective_from_is_before_the_next_day_in_utc() {
        PriceList list = new PriceList(UUID.randomUUID(), "Shop", Currency.of("EUR"), Channel.SHOP);
        Price lastMillisecond =
                list.price(PRODUCT, new Money(1, Currency.of("EUR")), Instant.parse("2026-10-19T23:59:59.999Z"));
        Price nextDay = list.price(PRODUCT, new Money(1, Currency.of("EUR")), Instant.parse("2026-10-20T00:00:00Z"));

        assertThat(lastMillisecond.appliesOn(LocalDate.parse("2026-10-19"))).isTrue();
        assertThat(lastMillisecond.appliesOn(LocalDate.parse("2026-10-18"))).isFalse();
        assertThat(nextDay.appliesOn(LocalDate.parse("2026-10-19"))).isFalse();
        assertThat(nextDay.appliesOn(LocalDate.parse("2026-10-20"))).isTrue();
    }

    @Test
    void the_amount_never_reaches_a_log_through_to_string() {
        Money money = new Money(1999, Currency.of("EUR"));

        assertThat(money.toString()).doesNotContain("1999");
    }
}
