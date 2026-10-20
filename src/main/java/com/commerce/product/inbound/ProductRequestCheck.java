package com.commerce.product.inbound;

import com.commerce.product.application.Fault;
import com.commerce.product.application.Identifiers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * The first two groups of a request of {@code product}, checked before Spring binds a parameter (AR-1): the headers,
 * then the query. Each group answers every field at fault in it; the first group refused stops the checks.
 */
@Component
class ProductRequestCheck implements HandlerInterceptor {

    static final String REQUEST_ID = "X-Request-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method) || !(method.getBean() instanceof ProductController)) {
            return true;
        }
        if (!Identifiers.isUuid(request.getHeader(REQUEST_ID))) {
            throw new RequestRefused(List.of(new Fault(REQUEST_ID, "The request identifier is missing or not a UUID.")));
        }
        List<Fault> faults = switch (method.getMethod().getName()) {
            case "listProducts" -> listQuery(request);
            default -> List.of();
        };
        if (!faults.isEmpty()) {
            throw new RequestRefused(faults);
        }
        return true;
    }

    /** The well-formed {@code X-Request-Id} of a request, or none: a malformed one is never logged (AR-2). */
    static String wellFormedRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID);
        return Identifiers.isUuid(requestId) ? requestId : null;
    }

    private static List<Fault> listQuery(HttpServletRequest request) {
        List<Fault> faults = new ArrayList<>();
        String localized = present(request.getParameter("localized"));
        if (localized != null && !localized.equals("true") && !localized.equals("false")) {
            faults.add(new Fault("localized", "The parameter is not a boolean."));
        }
        integer(request, "page", 0, Integer.MAX_VALUE, faults);
        integer(request, "size", 1, 100, faults);
        return faults;
    }

    /** An integer of the schema's type, within its bounds; beyond the integer range is refused (product BR-7). */
    private static void integer(HttpServletRequest request, String name, int min, int max, List<Fault> faults) {
        String value = present(request.getParameter(name));
        if (value == null) {
            return;
        }
        try {
            long number = Long.parseLong(value);
            if (number < min || number > max) {
                faults.add(new Fault(name, "The parameter is out of its bounds."));
            }
        } catch (NumberFormatException e) {
            faults.add(new Fault(name, "The parameter is not an integer."));
        }
    }

    /** An empty value takes the parameter's default, as Spring binds it. */
    private static String present(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
