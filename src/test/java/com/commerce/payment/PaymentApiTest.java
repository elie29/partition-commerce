package com.commerce.payment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A test of the HTTP API of {@code payment}, through a real server, on an in-memory database so that a run never
 * writes to the file database of {@code application.yaml}; one customer is under fraud review (AR-3).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:payment;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "commerce.fraud-review.customers[0]=" + PaymentApiTest.UNDER_REVIEW
        })
public @interface PaymentApiTest {

    /** The one customer under fraud review in a test of {@code payment}. */
    String UNDER_REVIEW = "customer-under-review";
}
