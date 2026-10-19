package com.commerce.invoice.inbound;

import com.commerce.invoice.application.CancelInvoiceUseCase;
import com.commerce.invoice.application.GetInvoiceUseCase;
import com.commerce.invoice.application.InvoiceView;
import com.commerce.invoice.application.IssueInvoiceCommand;
import com.commerce.invoice.application.IssueInvoiceUseCase;
import com.commerce.invoice.application.LineData;
import com.commerce.invoice.application.PayInvoiceUseCase;
import com.commerce.invoice.inbound.api.InvoicesApi;
import com.commerce.invoice.inbound.api.model.Invoice;
import com.commerce.invoice.inbound.api.model.IssueInvoice;
import com.commerce.invoice.inbound.api.model.Line;
import com.commerce.invoice.inbound.api.model.Money;
import com.commerce.invoice.inbound.api.model.PayInvoice;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** The HTTP API of {@code invoice}, as `docs/contracts/billing.openapi.json` declares it. */
@RestController
public class InvoiceController implements InvoicesApi {

    private final IssueInvoiceUseCase issueInvoice;
    private final GetInvoiceUseCase getInvoice;
    private final CancelInvoiceUseCase cancelInvoice;
    private final PayInvoiceUseCase payInvoice;

    public InvoiceController(
            IssueInvoiceUseCase issueInvoice,
            GetInvoiceUseCase getInvoice,
            CancelInvoiceUseCase cancelInvoice,
            PayInvoiceUseCase payInvoice) {
        this.issueInvoice = issueInvoice;
        this.getInvoice = getInvoice;
        this.cancelInvoice = cancelInvoice;
        this.payInvoice = payInvoice;
    }

    @Override
    public ResponseEntity<Invoice> getInvoice(String invoiceId) {
        return ResponseEntity.ok(toInvoice(getInvoice.read(invoiceId)));
    }

    @Override
    public ResponseEntity<Invoice> cancelInvoice(String invoiceId) {
        return ResponseEntity.ok(toInvoice(cancelInvoice.cancel(invoiceId)));
    }

    /** The request model prints its card token: it is handed to the use case and never logged (BR-3). */
    @Override
    public ResponseEntity<Invoice> payInvoice(String invoiceId, PayInvoice request) {
        return ResponseEntity.ok(toInvoice(payInvoice.pay(invoiceId, request.getCardToken())));
    }

    @Override
    public ResponseEntity<Invoice> issueInvoice(IssueInvoice request) {
        InvoiceView issued = issueInvoice.issue(new IssueInvoiceCommand(
                request.getOrderId(), request.getCustomerId(), toLineData(request.getLines())));
        return ResponseEntity.status(HttpStatus.CREATED).body(toInvoice(issued));
    }

    private static List<LineData> toLineData(List<Line> lines) {
        return lines.stream()
                .map(l -> l == null ? null
                        : new LineData(l.getLabel(), l.getPrice().getAmountMinor(), l.getPrice().getCurrency()))
                .toList();
    }

    private static Invoice toInvoice(InvoiceView view) {
        return new Invoice()
                .invoiceId(view.invoiceId().toString())
                .orderId(view.orderId())
                .status(Invoice.StatusEnum.fromValue(view.status()))
                .amount(new Money(view.amountMinor(), view.currency()))
                .lines(view.lines().stream()
                        .map(l -> new Line(l.label(), new Money(l.priceMinor(), l.currency())))
                        .toList());
    }
}
