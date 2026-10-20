package com.commerce.invoice.inbound;

import static com.commerce.invoice.inbound.InvoiceModels.toInvoice;

import com.commerce.invoice.application.CancelInvoiceUseCase;
import com.commerce.invoice.application.GetInvoiceUseCase;
import com.commerce.invoice.application.InvoiceView;
import com.commerce.invoice.application.IssueInvoiceCommand;
import com.commerce.invoice.application.IssueInvoiceUseCase;
import com.commerce.invoice.application.LineData;
import com.commerce.invoice.inbound.api.InvoiceApi;
import com.commerce.invoice.inbound.api.model.Invoice;
import com.commerce.invoice.inbound.api.model.IssueInvoice;
import com.commerce.invoice.inbound.api.model.Line;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** The operations of the tag {@code Invoice} of `docs/contracts/billing.openapi.json`. */
@RestController
public class InvoiceController implements InvoiceApi {

    private final IssueInvoiceUseCase issueInvoice;
    private final GetInvoiceUseCase getInvoice;
    private final CancelInvoiceUseCase cancelInvoice;

    public InvoiceController(
            IssueInvoiceUseCase issueInvoice, GetInvoiceUseCase getInvoice, CancelInvoiceUseCase cancelInvoice) {
        this.issueInvoice = issueInvoice;
        this.getInvoice = getInvoice;
        this.cancelInvoice = cancelInvoice;
    }

    @Override
    public ResponseEntity<Invoice> getInvoice(String invoiceId) {
        return ResponseEntity.ok(toInvoice(getInvoice.read(invoiceId)));
    }

    @Override
    public ResponseEntity<Invoice> cancelInvoice(String invoiceId) {
        return ResponseEntity.ok(toInvoice(cancelInvoice.cancel(invoiceId)));
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
}
