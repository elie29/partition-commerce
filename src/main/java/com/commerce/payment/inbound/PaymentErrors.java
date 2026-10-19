package com.commerce.payment.inbound;

import com.commerce.payment.application.CardDeclined;
import com.commerce.payment.application.InvoiceNotFound;
import com.commerce.payment.application.PayRefused;
import com.commerce.payment.application.PaymentContentRefused;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/** The refusals of the API of {@code payment}: a status and no body, so no value of the request travels back. */
@RestControllerAdvice(assignableTypes = PayInvoiceController.class)
class PaymentErrors {

    /** A request the contract refuses, or a card token BR-22 refuses. */
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class,
        HandlerMethodValidationException.class,
        PaymentContentRefused.class
    })
    ResponseEntity<Void> refused() {
        return ResponseEntity.badRequest().build();
    }

    /** BR-20, and a simultaneous pay or cancel recorded first (BR-5). */
    @ExceptionHandler(PayRefused.class)
    ResponseEntity<Void> payRefused() {
        return ResponseEntity.status(409).build();
    }

    /** BR-5: CardPay declined the card. */
    @ExceptionHandler(CardDeclined.class)
    ResponseEntity<Void> cardDeclined() {
        return ResponseEntity.status(402).build();
    }

    /** BR-21. */
    @ExceptionHandler(InvoiceNotFound.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }
}
