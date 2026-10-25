package co.edu.escuelaing.orders.http;

import co.edu.escuelaing.orders.model.Order;
import co.edu.escuelaing.orders.repository.InMemoryOrderRepository;
import co.edu.escuelaing.orders.service.OrderService;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrdersApiHandlerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OrdersApiHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OrdersApiHandler(new OrderService(InMemoryOrderRepository.withSampleData()));
    }

    // ---------- helpers ----------

    private APIGatewayProxyResponseEvent call(APIGatewayProxyRequestEvent event) {
        return handler.handleRequest(event, null);
    }

    private static APIGatewayProxyRequestEvent getById(String id) {
        return new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET").withResource("/orders/{id}").withPath("/orders/" + id)
                .withPathParameters(Map.of("id", id));
    }

    private static APIGatewayProxyRequestEvent search(Map<String, String> query) {
        return new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET").withResource("/orders").withPath("/orders")
                .withQueryStringParameters(query);
    }

    private static APIGatewayProxyRequestEvent post(String body) {
        return new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST").withResource("/orders").withPath("/orders")
                .withBody(body);
    }

    private static JsonNode body(APIGatewayProxyResponseEvent response) throws Exception {
        return MAPPER.readTree(response.getBody());
    }

    // ---------- GET /orders/{id} ----------

    @Test
    void getExistingOrderReturns200() throws Exception {
        APIGatewayProxyResponseEvent response = call(getById("ORD-001"));

        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertEquals("MacBook Air", body(response).get("product").asText());
    }

    @Test
    void getMissingOrderReturns404WithErrorBody() throws Exception {
        APIGatewayProxyResponseEvent response = call(getById("ORD-999"));

        assertEquals(404, response.getStatusCode());
        assertEquals("ORDER_NOT_FOUND", body(response).get("error").asText());
        assertEquals("Order ORD-999 does not exist", body(response).get("message").asText());
    }

    // ---------- GET /orders?customerId= ----------

    @Test
    void searchByCustomerReturns200WithOrders() throws Exception {
        APIGatewayProxyResponseEvent response = call(search(Map.of("customerId", "CUST-42")));

        assertEquals(200, response.getStatusCode());
        assertEquals(2, body(response).size());
    }

    @Test
    void searchForCustomerWithoutOrdersReturnsEmptyList() throws Exception {
        APIGatewayProxyResponseEvent response = call(search(Map.of("customerId", "CUST-100")));

        assertEquals(200, response.getStatusCode());
        assertEquals(0, body(response).size());
    }

    @Test
    void searchWithoutCustomerIdReturns400() throws Exception {
        APIGatewayProxyResponseEvent response = call(search(null)); // API Gateway sends null when there is no query string

        assertEquals(400, response.getStatusCode());
        assertEquals("INVALID_REQUEST", body(response).get("error").asText());
    }

    // ---------- POST /orders ----------

    @Test
    void createValidOrderReturns201WithLocation() throws Exception {
        APIGatewayProxyResponseEvent response =
                call(post("{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":1}"));

        assertEquals(201, response.getStatusCode());
        String id = body(response).get("id").asText();
        assertEquals("/orders/" + id, response.getHeaders().get("Location"));
    }

    @Test
    void createdOrderCanBeRetrievedThroughTheApi() throws Exception {
        Order created = MAPPER.readValue(
                call(post("{\"customerId\":\"CUST-9\",\"product\":\"Monitor\",\"quantity\":2}")).getBody(),
                Order.class);

        APIGatewayProxyResponseEvent response = call(getById(created.getId()));

        assertEquals(200, response.getStatusCode());
        assertEquals("Monitor", body(response).get("product").asText());
    }

    @Test
    void createWithInvalidQuantityReturns400() throws Exception {
        APIGatewayProxyResponseEvent response =
                call(post("{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":0}"));

        assertEquals(400, response.getStatusCode());
        assertEquals("INVALID_REQUEST", body(response).get("error").asText());
    }

    @Test
    void createWithWrongTypeReturns400() throws Exception {
        APIGatewayProxyResponseEvent response =
                call(post("{\"customerId\":\"CUST-42\",\"product\":\"MacBook Air\",\"quantity\":\"two\"}"));

        assertEquals(400, response.getStatusCode());
    }

    @Test
    void createWithMalformedJsonReturns400() {
        assertEquals(400, call(post("{not json")).getStatusCode());
    }

    @Test
    void createWithEmptyBodyReturns400() {
        assertEquals(400, call(post(null)).getStatusCode());
    }

    // ---------- other cases ----------

    @Test
    void unknownRouteReturns404() throws Exception {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent()
                .withHttpMethod("PATCH").withResource("/orders/{id}").withPath("/orders/ORD-001");

        APIGatewayProxyResponseEvent response = call(event);

        assertEquals(404, response.getStatusCode());
        assertEquals("ROUTE_NOT_FOUND", body(response).get("error").asText());
    }

    @Test
    void unexpectedErrorReturns500WithoutInternals() throws Exception {
        OrderService failing = new OrderService(new InMemoryOrderRepository()) {
            @Override
            public Order getOrder(String id) {
                throw new IllegalStateException("database exploded");
            }
        };
        APIGatewayProxyResponseEvent response =
                new OrdersApiHandler(failing).handleRequest(getById("ORD-001"), null);

        assertEquals(500, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", body(response).get("error").asText());
        assertFalse(response.getBody().contains("database exploded"));
    }

    private static APIGatewayProxyRequestEvent delete(String id) {
        return new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE").withResource("/orders/{id}").withPath("/orders/" + id)
                .withPathParameters(Map.of("id", id));
    }

    // ---------- DELETE /orders/{id} ----------

    @Test
    void deleteExistingOrderReturns204WithoutBody() {
        APIGatewayProxyResponseEvent response = call(delete("ORD-001"));

        assertEquals(204, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deletedOrderIsNoLongerRetrievable() {
        call(delete("ORD-001"));

        assertEquals(404, call(getById("ORD-001")).getStatusCode());
    }

    @Test
    void deleteMissingOrderReturns404() throws Exception {
        APIGatewayProxyResponseEvent response = call(delete("ORD-999"));

        assertEquals(404, response.getStatusCode());
        assertEquals("ORDER_NOT_FOUND", body(response).get("error").asText());
    }

    @Test
    void deletingTwiceReturns404TheSecondTime() {
        assertEquals(204, call(delete("ORD-002")).getStatusCode());
        assertEquals(404, call(delete("ORD-002")).getStatusCode());
    }
}
