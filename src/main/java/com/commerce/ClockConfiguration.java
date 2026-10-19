package com.commerce;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The server's clock, in UTC: "now" of the catalog (`docs/specs/catalog/`, AR-5), replaced by a test that sets the time. */
@Configuration(proxyBeanMethods = false)
class ClockConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
