package co.edu.escuelaing.orders.http;

import co.edu.escuelaing.orders.model.CreateOrderRequest;
import co.edu.escuelaing.orders.model.Order;
import co.edu.escuelaing.orders.repository.InMemoryOrderRepository;
import co.edu.escuelaing.orders.service.InvalidOrderException;
import co.edu.escuelaing.orders.service.OrderNotFoundException;
import co.edu.escuelaing.orders.service.OrderService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP adapter for Architecture B (Lambda proxy integration).
 * This is the ONLY class that knows about API Gateway events, HTTP status
 * codes, headers and JSON serialization. Business rules stay in OrderService.
 */
public class OrdersApiHandler
        implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    // Created once per execution environment (cold start) and reused while warm.
    private static final OrderService DEFAULT_SERVICE =
            new OrderService(InMemoryOrderRepository.withSampleData());

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final OrderService service;

    /** Used by the Lambda runtime. */
    public OrdersApiHandler() {
        this(DEFAULT_SERVICE);
    }

    /** Used by tests to inject a fresh service. */
    public OrdersApiHandler(OrderService service) {
        this.service = service;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            return route(event);
        } catch (InvalidOrderException e) {
            return error(400, "INVALID_REQUEST", e.getMessage());
        } catch (OrderNotFoundException e) {
            return error(404, "ORDER_NOT_FOUND", e.getMessage());
        } catch (Exception e) {
            // Real cause goes to CloudWatch; the client only gets a generic message.
            if (context != null) {
                context.getLogger().log("Unexpected error: " + e);
            }
            return error(500, "INTERNAL_ERROR", "An unexpected error occurred");
        }
    }

    // ---------- routing ----------

    private APIGatewayProxyResponseEvent route(APIGatewayProxyRequestEvent event) {
        String method = event.getHttpMethod();
        String resource = event.getResource(); // route template, e.g. "/orders/{id}"

        if ("POST".equals(method) && "/orders".equals(resource)) {
            return createOrder(event);
        }
        if ("GET".equals(method) && "/orders".equals(resource)) {
            return findOrdersByCustomer(event);
        }
        if ("GET".equals(method) && "/orders/{id}".equals(resource)) {
            return getOrder(event);
        }
        return error(404, "ROUTE_NOT_FOUND", "No route for " + method + " " + event.getPath());
    }

    // ---------- operations ----------

    private APIGatewayProxyResponseEvent createOrder(APIGatewayProxyRequestEvent event) {
        CreateOrderRequest request;
        try {
            request = parseBody(event.getBody());
        } catch (JsonProcessingException e) {
            return error(400, "INVALID_REQUEST",
                    "Request body is not valid JSON or has fields with invalid types");
        }
        Order order = service.createOrder(request);
        return json(201, order, Map.of("Location", "/orders/" + order.getId()));
    }

    private APIGatewayProxyResponseEvent getOrder(APIGatewayProxyRequestEvent event) {
        String id = param(event.getPathParameters(), "id");
        return json(200, service.getOrder(id), Map.of());
    }

    private APIGatewayProxyResponseEvent findOrdersByCustomer(APIGatewayProxyRequestEvent event) {
        String customerId = param(event.getQueryStringParameters(), "customerId");
        List<Order> orders = service.getOrdersByCustomer(customerId);
        return json(200, orders, Map.of());
    }

    // ---------- helpers ----------

    private static CreateOrderRequest parseBody(String body) throws JsonProcessingException {
        if (body == null || body.isBlank()) {
            return null; // OrderService reports "Request body is required"
        }
        return MAPPER.readValue(body, CreateOrderRequest.class);
    }

    private static String param(Map<String, String> params, String name) {
        return params == null ? null : params.get(name);
    }

    private static APIGatewayProxyResponseEvent error(int status, String code, String message) {
        return json(status, new ErrorResponse(code, message), Map.of());
    }

    private static APIGatewayProxyResponseEvent json(int status, Object body, Map<String, String> extraHeaders) {
        Map<String, String> headers = new HashMap<>(extraHeaders);
        headers.put("Content-Type", "application/json");
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(status)
                .withHeaders(headers)
                .withBody(toJson(body));
    }

    private static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize response", e);
        }
    }
}