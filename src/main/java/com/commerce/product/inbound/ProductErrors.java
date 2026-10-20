package com.commerce.product.inbound;

import com.commerce.product.application.ProductNotFound;
import com.commerce.product.application.ReferenceAlreadyHeld;
import com.commerce.product.inbound.api.model.ListProducts400Response;
import com.commerce.product.inbound.api.model.ListProducts400ResponseErrorsInner;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The error answers of {@code product}, each with the common error envelope of AR-1 and one log line of AR-2: the
 * {@code errorId}, the {@code X-Request-Id} when well formed, the path, the status; never a value of the request (P-4:
 * one handler per context).
 */
@RestControllerAdvice(assignableTypes = ProductController.class)
class ProductErrors {

    private static final Logger LOG = LoggerFactory.getLogger(ProductErrors.class);

    private static final String VALIDATION_FAILED = "VALIDATION_FAILED";

    /** The headers, the query or the body refused: one item per field at fault in the first group refused. */
    @ExceptionHandler(RequestRefused.class)
    ResponseEntity<ListProducts400Response> refused(RequestRefused refused, HttpServletRequest request) {
        List<ListProducts400ResponseErrorsInner> items = refused.faults().stream()
                .map(fault -> item(VALIDATION_FAILED, fault.message(), fault.field()))
                .toList();
        return answer(request, HttpStatus.BAD_REQUEST, "The request is refused: a field is at fault.", items);
    }

    /** No body, or one that is not a JSON object: one item, no field. */
    @ExceptionHandler({BodyUnreadable.class, HttpMessageNotReadableException.class})
    ResponseEntity<ListProducts400Response> unreadable(HttpServletRequest request) {
        return answer(request, HttpStatus.BAD_REQUEST, "The request body cannot be read.", List.of(item(
                VALIDATION_FAILED, "The body is missing or is not a JSON object the contract accepts.", null)));
    }

    /** BR-1. */
    @ExceptionHandler(ReferenceAlreadyHeld.class)
    ResponseEntity<ListProducts400Response> conflict(HttpServletRequest request) {
        return answer(request, HttpStatus.CONFLICT, "A product already holds this reference.",
                List.of(item("CONFLICT", "The reference is held by another product.", "/reference")));
    }

    /** BR-8. */
    @ExceptionHandler(ProductNotFound.class)
    ResponseEntity<ListProducts400Response> productNotFound(HttpServletRequest request) {
        return answer(request, HttpStatus.NOT_FOUND, "No such product.",
                List.of(item("PRODUCT_NOT_FOUND", "No product holds this identifier.", null)));
    }

    /**
     * Any other failure: the envelope with its {@code detail} and {@code errorId}, {@code errors} empty (AR-1); the log
     * adds the class and the stack frames of the exception and of its causes, never a message (AR-2).
     */
    @ExceptionHandler(RuntimeException.class)
    ResponseEntity<ListProducts400Response> failure(RuntimeException failure, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        LOG.error("error errorId={} requestId={} path={} status={} exception={}", errorId,
                ProductRequestCheck.wellFormedRequestId(request), request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), framesOf(failure));
        return envelope(errorId, request, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected failure occurred.",
                List.of());
    }

    private static ResponseEntity<ListProducts400Response> answer(
            HttpServletRequest request,
            HttpStatus status,
            String detail,
            List<ListProducts400ResponseErrorsInner> items) {
        String errorId = UUID.randomUUID().toString();
        LOG.info("error errorId={} requestId={} path={} status={}", errorId,
                ProductRequestCheck.wellFormedRequestId(request), request.getRequestURI(), status.value());
        return envelope(errorId, request, status, detail, items);
    }

    /** The class and the frames of an exception and of each of its causes; a message never, it may carry a value. */
    private static String framesOf(Throwable failure) {
        StringBuilder frames = new StringBuilder();
        for (Throwable cause = failure; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            frames.append(cause == failure ? "" : "\ncaused by ").append(cause.getClass().getName());
            for (StackTraceElement frame : cause.getStackTrace()) {
                frames.append("\n\tat ").append(frame);
            }
        }
        return frames.toString();
    }

    private static ResponseEntity<ListProducts400Response> envelope(
            String errorId,
            HttpServletRequest request,
            HttpStatus status,
            String detail,
            List<ListProducts400ResponseErrorsInner> items) {
        ListProducts400Response envelope = new ListProducts400Response(
                errorId,
                request.getRequestURI(),
                OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS),
                detail);
        envelope.setErrors(items);
        return ResponseEntity.status(status).body(envelope);
    }

    private static ListProducts400ResponseErrorsInner item(String code, String message, String field) {
        return new ListProducts400ResponseErrorsInner(code, message).field(field);
    }
}
