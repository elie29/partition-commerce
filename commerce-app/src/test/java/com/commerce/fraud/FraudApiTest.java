package com.commerce.fraud;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A test of {@code fraud} through a real server, for {@code CustomerStatus} over HTTP, on an in-memory database so
 * that a run never writes to the file database of {@code application.yaml}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:fraud;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
public @interface FraudApiTest {
}
