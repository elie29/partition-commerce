package com.commerce.refund.inbound;

import com.commerce.refund.application.InvoiceNotFound;
import com.commerce.refund.application.RefundContentRefused;
import com.commerce.refund.application.RefundForbidden;
import com.commerce.refund.application.RefundRefused;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** The refusals of the API of {@code refund}: a status and no body, so no value of the request travels back. */
@RestControllerAdvice(assignableTypes = RefundInvoiceController.class)
class RefundErrors {

    /** A request the contract refuses, an amount BR-26 refuses, a currency other than the amount paid's (BR-25). */
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        RefundContentRefused.class
    })
    ResponseEntity<Void> refused() {
        return ResponseEntity.badRequest().build();
    }

    /** BR-27. */
    @ExceptionHandler(InvoiceNotFound.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    /** AR-3: the customer of the invoice is under fraud review. */
    @ExceptionHandler(RefundForbidden.class)
    ResponseEntity<Void> forbidden() {
        return ResponseEntity.status(403).build();
    }

    /** BR-25, BR-29: the invoice is not paid, or the refund exceeds what remains. */
    @ExceptionHandler(RefundRefused.class)
    ResponseEntity<Void> refundRefused() {
        return ResponseEntity.status(409).build();
    }
}
