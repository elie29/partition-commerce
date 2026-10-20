package com.commerce.invoice.inbound;

import com.commerce.invoice.application.CancelRefused;
import com.commerce.invoice.application.InvoiceContentRefused;
import com.commerce.invoice.application.InvoiceNotFound;
import com.commerce.invoice.application.OrderAlreadyInvoiced;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** The refusals of the API of {@code invoice}: a status and no body, so no value of the request travels back. */
@RestControllerAdvice(assignableTypes = InvoiceController.class)
class InvoiceErrors {

    /** A request the contract refuses, or a content a business rule refuses (BR-13). */
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        InvoiceContentRefused.class
    })
    ResponseEntity<Void> refused() {
        return ResponseEntity.badRequest().build();
    }

    /** BR-1. */
    @ExceptionHandler(OrderAlreadyInvoiced.class)
    ResponseEntity<Void> alreadyInvoiced() {
        return ResponseEntity.status(409).build();
    }

    /** BR-4, BR-18. */
    @ExceptionHandler(CancelRefused.class)
    ResponseEntity<Void> cancelRefused() {
        return ResponseEntity.status(409).build();
    }

    /** BR-15, BR-19. */
    @ExceptionHandler(InvoiceNotFound.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }
}
