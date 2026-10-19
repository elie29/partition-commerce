package com.commerce.refund;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A test of the HTTP API of {@code refund}, through a real server, on an in-memory database so that a run never
 * writes to the file database of {@code application.yaml}; one customer is under fraud review (AR-3).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "spring.datasource.url=jdbc:h2:mem:refund;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
            "commerce.fraud-review.customers[0]=" + RefundApiTest.UNDER_REVIEW
        })
public @interface RefundApiTest {

    /** The one customer under fraud review in a test of {@code refund}. */
    String UNDER_REVIEW = "customer-under-review";
}
