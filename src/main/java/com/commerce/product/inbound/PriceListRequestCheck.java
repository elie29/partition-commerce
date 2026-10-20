package com.commerce.product.inbound;

import com.commerce.product.application.Fault;
import com.commerce.product.application.Identifiers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * The first two groups of a request of {@code price-list}, checked before Spring binds a parameter (AR-1): its
 * headers, then its query. Only {@code getProductPrice} takes a query, its {@code at} (BR-24).
 */
@Component
class PriceListRequestCheck implements HandlerInterceptor {

    static final String REQUEST_ID = "X-Request-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method) || !(method.getBean() instanceof PriceListController)) {
            return true;
        }
        if (!Identifiers.isUuid(request.getHeader(REQUEST_ID))) {
            throw new RequestRefused(List.of(new Fault(REQUEST_ID, "The request identifier is missing or not a UUID.")));
        }
        if (method.getMethod().getName().equals("getProductPrice") && !isDateOrAbsent(request.getParameter("at"))) {
            throw new RequestRefused(List.of(new Fault("at", "The parameter is not a date.")));
        }
        return true;
    }

    /** The well-formed {@code X-Request-Id} of a request, or none: a malformed one is never logged (AR-2). */
    static String wellFormedRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID);
        return Identifiers.isUuid(requestId) ? requestId : null;
    }

    /** An empty value takes the parameter's default, as Spring binds it. */
    private static boolean isDateOrAbsent(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
