package com.commerce.discount.inbound;

import com.commerce.discount.application.Fault;
import com.commerce.discount.application.Identifiers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * The first group of a request of {@code discount}, checked before Spring binds a parameter (AR-1): its headers. Its
 * operations take no query.
 */
@Component
class DiscountRequestCheck implements HandlerInterceptor {

    static final String REQUEST_ID = "X-Request-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod method && method.getBean() instanceof DiscountController
                && !Identifiers.isUuid(request.getHeader(REQUEST_ID))) {
            throw new RequestRefused(List.of(new Fault(REQUEST_ID, "The request identifier is missing or not a UUID.")));
        }
        return true;
    }

    /** The well-formed {@code X-Request-Id} of a request, or none: a malformed one is never logged (AR-2). */
    static String wellFormedRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID);
        return Identifiers.isUuid(requestId) ? requestId : null;
    }
}
