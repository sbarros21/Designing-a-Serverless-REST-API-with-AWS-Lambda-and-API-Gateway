package co.edu.escuelaing.orders.service;

import co.edu.escuelaing.orders.model.CreateOrderRequest;
import co.edu.escuelaing.orders.model.Order;
import co.edu.escuelaing.orders.repository.InMemoryOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(InMemoryOrderRepository.withSampleData());
    }

    // ---------- create ----------

    @Test
    void createsValidOrder() {
        Order order = service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 1));

        assertEquals("ORD-004", order.getId());
        assertEquals("CUST-42", order.getCustomerId());
        assertEquals("MacBook Air", order.getProduct());
        assertEquals(1, order.getQuantity());
    }

    @Test
    void createdOrderCanBeRetrieved() {
        Order created = service.createOrder(new CreateOrderRequest("CUST-99", "Monitor", 3));

        assertEquals(created, service.getOrder(created.getId()));
    }

    @Test
    void generatesDifferentIdsForEachOrder() {
        Order first = service.createOrder(new CreateOrderRequest("CUST-1", "A", 1));
        Order second = service.createOrder(new CreateOrderRequest("CUST-1", "B", 1));

        assertNotEquals(first.getId(), second.getId());
    }

    // ---------- get by id ----------

    @Test
    void retrievesExistingOrder() {
        Order order = service.getOrder("ORD-003");

        assertEquals("CUST-7", order.getCustomerId());
        assertEquals("Keyboard", order.getProduct());
        assertEquals(2, order.getQuantity());
    }

    @Test
    void throwsWhenOrderDoesNotExist() {
        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> service.getOrder("ORD-999"));

        assertEquals("Order ORD-999 does not exist", ex.getMessage());
        assertEquals("ORD-999", ex.getOrderId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankOrderId(String id) {
        assertThrows(InvalidOrderException.class, () -> service.getOrder(id));
    }

    // ---------- search by customer ----------

    @Test
    void findsOrdersByCustomer() {
        List<Order> orders = service.getOrdersByCustomer("CUST-42");

        assertEquals(2, orders.size());
        assertTrue(orders.stream().allMatch(o -> o.getCustomerId().equals("CUST-42")));
    }

    @Test
    void returnsEmptyListForCustomerWithoutOrders() {
        assertTrue(service.getOrdersByCustomer("CUST-100").isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankCustomerIdInSearch(String customerId) {
        assertThrows(InvalidOrderException.class, () -> service.getOrdersByCustomer(customerId));
    }

    // ---------- invalid input on create ----------

    @Test
    void rejectsNullRequest() {
        assertThrows(InvalidOrderException.class, () -> service.createOrder(null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankCustomerId(String customerId) {
        assertThrows(InvalidOrderException.class,
                () -> service.createOrder(new CreateOrderRequest(customerId, "MacBook Air", 1)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsBlankProduct(String product) {
        assertThrows(InvalidOrderException.class,
                () -> service.createOrder(new CreateOrderRequest("CUST-42", product, 1)));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rejectsInvalidQuantity(Integer quantity) {
        assertThrows(InvalidOrderException.class,
                () -> service.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", quantity)));
    }
}
