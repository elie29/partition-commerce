package com.commerce.payment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A test of the HTTP API of {@code refund}, through a real server, on an in-memory database so that a run never
 * writes to the file database of {@code application.yaml}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:refund;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
public @interface RefundApiTest {

    /** The customer a test of {@code refund} places under fraud review on {@link CommerceAppStub} (BR-32). */
    String UNDER_REVIEW = "customer-under-review";
}
