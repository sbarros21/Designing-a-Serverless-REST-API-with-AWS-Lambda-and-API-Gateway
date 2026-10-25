package co.edu.escuelaing.orders.lambda;

import co.edu.escuelaing.orders.model.CreateOrderRequest;
import co.edu.escuelaing.orders.model.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderFunctionsTest {

    private final OrderFunctions functions = new OrderFunctions();

    @Test
    void returnsExistingOrder() {
        Order order = functions.getOrder(new OrderIdRequest("ORD-001"));
        assertEquals("MacBook Air", order.getProduct());
    }

    @Test
    void missingOrderIsReportedWithNotFoundCode() {
        FunctionError error = assertThrows(FunctionError.class,
                () -> functions.getOrder(new OrderIdRequest("ORD-999")));

        assertEquals("ORDER_NOT_FOUND", error.getCode());
        assertEquals("[ORDER_NOT_FOUND] Order ORD-999 does not exist", error.getMessage());
    }

    @Test
    void invalidInputIsReportedWithInvalidRequestCode() {
        FunctionError error = assertThrows(FunctionError.class,
                () -> functions.createOrder(new CreateOrderRequest("CUST-42", "MacBook Air", 0)));

        assertEquals("INVALID_REQUEST", error.getCode());
        assertTrue(error.getMessage().startsWith("[INVALID_REQUEST] "));
    }

    @Test
    void missingCustomerIdIsReportedWithInvalidRequestCode() {
        FunctionError error = assertThrows(FunctionError.class,
                () -> functions.findOrdersByCustomer(new CustomerOrdersRequest(null)));

        assertEquals("INVALID_REQUEST", error.getCode());
    }
}