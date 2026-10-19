package com.commerce.payment;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

/**
 * The migrations of the fission of {@code invoice} (V4, V5), on a database at V3 that holds a paid invoice: its payment
 * moves to {@code payment}'s table and leaves the invoice row. It tests no use case.
 */
class PaymentMigrationTest {

    private static final String URL = "jdbc:h2:mem:migration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1";

    @Test
    void copies_a_payment_recorded_on_the_invoice_row_into_the_payment_table() throws SQLException {
        migrate("3");
        UUID invoiceId = UUID.randomUUID();
        try (Connection db = DriverManager.getConnection(URL, "sa", ""); Statement sql = db.createStatement()) {
            sql.execute("INSERT INTO invoice (invoice_id, order_id, customer_id, status, charge_reference, paid_at)"
                    + " VALUES ('" + invoiceId + "', 'order-1', 'customer-1', 'PAID', 'ref-1',"
                    + " TIMESTAMP WITH TIME ZONE '2026-10-19 10:00:00+00')");
            sql.execute("INSERT INTO invoice_line VALUES ('" + invoiceId + "', 0, 'Book', 1250, 'EUR')");
            sql.execute("INSERT INTO invoice_line VALUES ('" + invoiceId + "', 1, 'Pen', 199, 'EUR')");
        }

        migrate("latest");

        try (Connection db = DriverManager.getConnection(URL, "sa", "");
                Statement sql = db.createStatement();
                ResultSet payment = sql.executeQuery("SELECT amount_minor, amount_currency, charge_reference,"
                        + " paid_at FROM payment WHERE invoice_id = '" + invoiceId + "'")) {
            assertThat(payment.next()).isTrue();
            assertThat(payment.getLong("amount_minor")).isEqualTo(1449);
            assertThat(payment.getString("amount_currency")).isEqualTo("EUR");
            assertThat(payment.getString("charge_reference")).isEqualTo("ref-1");
            assertThat(payment.getObject("paid_at", java.time.OffsetDateTime.class).toInstant())
                    .isEqualTo(java.time.Instant.parse("2026-10-19T10:00:00Z"));
            assertThat(payment.next()).isFalse();
        }
        try (Connection db = DriverManager.getConnection(URL, "sa", "");
                ResultSet columns = db.getMetaData().getColumns(null, null, "INVOICE", null)) {
            java.util.List<String> names = new java.util.ArrayList<>();
            while (columns.next()) {
                names.add(columns.getString("COLUMN_NAME"));
            }
            assertThat(names).doesNotContain("CHARGE_REFERENCE", "PAID_AT").contains("STATUS");
        }
    }

    private static void migrate(String target) {
        Flyway.configure()
                .dataSource(URL, "sa", "")
                .locations("classpath:db/migration")
                .target(target)
                .load()
                .migrate();
    }
}
