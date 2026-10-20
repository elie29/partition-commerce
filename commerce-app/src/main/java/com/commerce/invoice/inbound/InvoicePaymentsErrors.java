package com.commerce.invoice.inbound;

import com.commerce.invoice.application.InvoiceNotFound;
import com.commerce.invoice.application.InvoiceNotIssued;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** The refusals of {@code InvoicePayments} over HTTP: a status and no body. */
@RestControllerAdvice(assignableTypes = InvoicePaymentsController.class)
class InvoicePaymentsErrors {

    /** Not a UUID, or no invoice (architect, 2026-10-20). */
    @ExceptionHandler(InvoiceNotFound.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    /** BR-23: the invoice is no longer {@code ISSUED}; nothing is recorded. */
    @ExceptionHandler(InvoiceNotIssued.class)
    ResponseEntity<Void> notIssued() {
        return ResponseEntity.status(409).build();
    }
}
