package com.commerce.invoice.application;

import com.commerce.invoice.domain.InvalidInvoice;
import com.commerce.invoice.domain.Invoice;
import com.commerce.invoice.domain.Line;
import com.commerce.invoice.domain.Money;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** The accountant issues the invoice of a confirmed order (REQ-1). */
@Service
public class IssueInvoiceUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(IssueInvoiceUseCase.class);

    private final Invoices invoices;

    public IssueInvoiceUseCase(Invoices invoices) {
        this.invoices = invoices;
    }

    /**
     * @throws InvoiceContentRefused when the content breaks a business rule, before BR-1 is checked (BR-13)
     * @throws OrderAlreadyInvoiced when the order already has an invoice that is not cancelled (BR-1)
     */
    public InvoiceView issue(IssueInvoiceCommand command) {
        Invoice invoice;
        try {
            invoice = Invoice.issue(command.orderId(), command.customerId(), toLines(command.lines()));
        } catch (InvalidInvoice e) {
            throw new InvoiceContentRefused(e.getMessage(), e);
        }
        invoices.add(invoice);
        LOG.info("Invoice {} issued for order {}", invoice.id(), invoice.orderId());
        return InvoiceView.of(invoice);
    }

    private static List<Line> toLines(List<LineData> lines) {
        if (lines == null) {
            return null;
        }
        return lines.stream()
                .map(l -> l == null ? null : new Line(l.label(), new Money(l.priceMinor(), l.currency())))
                .toList();
    }
}
