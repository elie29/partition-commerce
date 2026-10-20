package com.commerce.invoice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A test of the HTTP API of {@code invoice}, through a real server, on an in-memory database so that a run never
 * writes to the file database of {@code application.yaml}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:invoice;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
public @interface InvoiceApiTest {
}
