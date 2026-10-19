package com.commerce.invoice.outbound;

import com.commerce.invoice.application.Invoices;
import com.commerce.invoice.application.OrderAlreadyInvoiced;
import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.InvoiceStatus;
import com.commerce.invoice.domain.Line;
import com.commerce.invoice.domain.Money;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** The invoices in the database, through {@link JdbcClient}; BR-1 is held by the unique key on the order of the invoices not cancelled (V2). */
@Repository
class JdbcInvoices implements Invoices {

    private final JdbcClient jdbc;

    JdbcInvoices(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void add(Invoice invoice) {
        try {
            jdbc.sql("INSERT INTO invoice (invoice_id, order_id, customer_id, status) VALUES (?, ?, ?, ?)")
                    .params(invoice.id(), invoice.orderId(), invoice.customerId(), invoice.status().name())
                    .update();
        } catch (DuplicateKeyException e) {
            throw new OrderAlreadyInvoiced(e);
        }
        List<Line> lines = invoice.lines();
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            jdbc.sql("INSERT INTO invoice_line (invoice_id, line_no, label, price_minor, price_currency)"
                            + " VALUES (?, ?, ?, ?, ?)")
                    .params(invoice.id(), i, line.label(), line.price().amountMinor(), line.price().currency())
                    .update();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Invoice> find(UUID id) {
        Optional<Header> header = jdbc.sql("SELECT order_id, customer_id, status FROM invoice WHERE invoice_id = ?")
                .param(id)
                .query((rs, n) -> new Header(rs.getString("order_id"), rs.getString("customer_id"),
                        rs.getString("status")))
                .optional();
        return header.map(h -> Invoice.restore(id, h.orderId(), h.customerId(), InvoiceStatus.valueOf(h.status()),
                jdbc.sql("SELECT label, price_minor, price_currency FROM invoice_line WHERE invoice_id = ? ORDER BY line_no")
                        .param(id)
                        .query((rs, n) -> new Line(rs.getString("label"),
                                new Money(rs.getLong("price_minor"), rs.getString("price_currency"))))
                        .list()));
    }

    @Override
    @Transactional
    public boolean changeStatus(UUID id, InvoiceStatus from, InvoiceStatus to) {
        return jdbc.sql("UPDATE invoice SET status = ? WHERE invoice_id = ? AND status = ?")
                .params(to.name(), id, from.name())
                .update() == 1;
    }

    private record Header(String orderId, String customerId, String status) {
    }
}
