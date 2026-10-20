package com.commerce.payment;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code commerce-app} faked entirely for the tests of {@code payment-app} (TL, 2026-10-20): an HTTP server on the
 * JDK's {@link HttpServer}, which the real generated client calls. It answers the five operations of
 * `docs/contracts/billing-internal.openapi.json` 1.0.0 as the contract says, and the issue, read and cancel of
 * {@code invoice} that a test needs to hold an invoice. A test places and lifts a review, and makes an operation slow,
 * failing, or {@code commerce-app} unreachable.
 */
final class CommerceAppStub {

    /** One server for the run; each test class points {@code commerce.billing-internal.base-url} at it. */
    static final CommerceAppStub INSTANCE = new CommerceAppStub();

    /** Longer than the 2 seconds after which {@code payment-app} gives up (BR-5, BR-27, BR-32). */
    private static final long SLOW_MILLIS = 2_500;

    private static final Pattern UUID_FORM =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final Pattern INVOICE = Pattern.compile("/(internal/)?invoices/([^/]+)(/[a-z-]+)?");
    private static final Pattern CUSTOMER_STATUS = Pattern.compile("/internal/customers/(.+)/status");

    private final JsonMapper json = JsonMapper.builder().build();
    private final Map<UUID, ObjectNode> invoices = new ConcurrentHashMap<>();
    private final Map<UUID, String> customers = new ConcurrentHashMap<>();
    private final Set<String> blocked = ConcurrentHashMap.newKeySet();
    private final Set<String> slow = ConcurrentHashMap.newKeySet();
    private final Set<String> failing = ConcurrentHashMap.newKeySet();
    private final int port;
    private HttpServer server;

    private CommerceAppStub() {
        server = start(0);
        port = server.getAddress().getPort();
    }

    String baseUrl() {
        return "http://localhost:" + port;
    }

    /** As the risk team places a review: the customer, matched as the same text, is blocked. */
    void place(String customerId) {
        blocked.add(customerId);
    }

    void lift(String customerId) {
        blocked.remove(customerId);
    }

    /** The operation of 1.0.0, by its {@code operationId}, answers after more than 2 seconds. */
    void slow(String operationId) {
        slow.add(operationId);
    }

    /** The operation of 1.0.0, by its {@code operationId}, answers 500. */
    void failing(String operationId) {
        failing.add(operationId);
    }

    /** {@code commerce-app} stops answering: a connection is refused. */
    synchronized void unreachable() {
        server.stop(0);
        server = null;
    }

    /** Back to answering every operation at once, on the same port; the invoices and reviews stay. */
    synchronized void reset() {
        slow.clear();
        failing.clear();
        if (server == null) {
            server = start(port);
        }
    }

    private HttpServer start(int on) {
        try {
            HttpServer started = HttpServer.create(new InetSocketAddress("localhost", on), 0);
            started.setExecutor(Executors.newCachedThreadPool());
            started.createContext("/", this::answer);
            started.start();
            return started;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void answer(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            Matcher customer = CUSTOMER_STATUS.matcher(path);
            Matcher invoice = INVOICE.matcher(path);
            if ("POST".equals(method) && "/invoices".equals(path)) {
                send(exchange, 201, issue(json.readTree(exchange.getRequestBody())));
            } else if ("GET".equals(method) && customer.matches()) {
                if (delayed(exchange, "getCustomerStatus")) {
                    return;
                }
                send(exchange, 200, json.createObjectNode().put("blocked", blocked.contains(customer.group(1))));
            } else if (invoice.matches()) {
                boolean internal = invoice.group(1) != null;
                String operation = operation(method, internal, invoice.group(3));
                if (operation == null) {
                    send(exchange, 404, null);
                } else if (!internal || !delayed(exchange, operation)) {
                    invoice(exchange, operation, invoice.group(2));
                }
            } else {
                send(exchange, 404, null);
            }
        }
    }

    private static String operation(String method, boolean internal, String suffix) {
        String key = method + " " + (internal ? "internal" : "public") + " " + (suffix == null ? "" : suffix);
        return switch (key) {
            case "GET internal " -> "getInvoiceForPayment";
            case "GET internal /customer" -> "getInvoiceCustomer";
            case "POST internal /payment" -> "recordPayment";
            case "POST internal /payment-cancellation" -> "recordPaymentCancelled";
            case "GET public " -> "getInvoice";
            case "POST public /cancel" -> "cancelInvoice";
            default -> null;
        };
    }

    /** True when the operation answered 500, or the client gave up waiting. */
    private boolean delayed(HttpExchange exchange, String operation) throws IOException {
        if (slow.contains(operation)) {
            try {
                Thread.sleep(SLOW_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (failing.contains(operation)) {
            send(exchange, 500, null);
            return true;
        }
        return false;
    }

    private void invoice(HttpExchange exchange, String operation, String invoiceId) throws IOException {
        if ("recordPaymentCancelled".equals(operation)) {
            send(exchange, 501, null);
            return;
        }
        Optional<UUID> id = Optional.of(invoiceId).filter(i -> UUID_FORM.matcher(i).matches()).map(UUID::fromString)
                .filter(invoices::containsKey);
        if (id.isEmpty()) {
            send(exchange, 404, null);
            return;
        }
        ObjectNode found = invoices.get(id.get());
        switch (operation) {
            case "getInvoiceCustomer" ->
                send(exchange, 200, json.createObjectNode().put("customerId", customers.get(id.get())));
            case "recordPayment" -> send(exchange, change(found, "PAID") ? 200 : 409, found);
            case "cancelInvoice" -> send(exchange, change(found, "CANCELLED") ? 200 : 409, found);
            default -> send(exchange, 200, found);
        }
    }

    /** The first change of an invoice still {@code ISSUED} holds, as {@code invoice} decides (BR-23). */
    private static boolean change(ObjectNode invoice, String status) {
        synchronized (invoice) {
            if (!"ISSUED".equals(invoice.get("status").asString())) {
                return false;
            }
            invoice.put("status", status);
            return true;
        }
    }

    private ObjectNode issue(JsonNode request) {
        UUID id = UUID.randomUUID();
        ArrayNode lines = (ArrayNode) request.get("lines");
        long total = 0;
        for (JsonNode line : lines) {
            total += line.get("price").get("amountMinor").asLong();
        }
        ObjectNode invoice = json.createObjectNode()
                .put("invoiceId", id.toString())
                .put("orderId", request.get("orderId").asString())
                .put("status", "ISSUED");
        invoice.putObject("amount").put("amountMinor", total)
                .put("currency", lines.get(0).get("price").get("currency").asString());
        invoice.set("lines", lines.deepCopy());
        invoices.put(id, invoice);
        customers.put(id, request.get("customerId").asString());
        return invoice;
    }

    private void send(HttpExchange exchange, int status, JsonNode body) throws IOException {
        if (body == null || status >= 400) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        byte[] bytes;
        synchronized (body) {
            bytes = json.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
