package co.edu.escuelaing.orders.lambda;

import co.edu.escuelaing.orders.model.CreateOrderRequest;
import co.edu.escuelaing.orders.model.Order;
import co.edu.escuelaing.orders.repository.InMemoryOrderRepository;
import co.edu.escuelaing.orders.service.InvalidOrderException;
import co.edu.escuelaing.orders.service.OrderNotFoundException;
import co.edu.escuelaing.orders.service.OrderService;

import java.util.List;
import java.util.function.Supplier;

/**
 * Lambda entry points (Architecture A: non-proxy integration).
 * Plain Java: no AWS classes. The Lambda Java runtime converts the
 * incoming JSON into the parameter type and the return value back into JSON.
 * Domain exceptions are translated into errors prefixed with a contract
 * error code, which API Gateway maps to HTTP status codes.
 */
public class OrderFunctions {

    // Created once per execution environment (cold start) and reused while it stays warm.
    private static final OrderService SERVICE =
            new OrderService(InMemoryOrderRepository.withSampleData());

    public Order createOrder(CreateOrderRequest request) {
        return handle(() -> SERVICE.createOrder(request));
    }

    public Order getOrder(OrderIdRequest request) {
        return handle(() -> SERVICE.getOrder(request == null ? null : request.getId()));
    }

    public List<Order> findOrdersByCustomer(CustomerOrdersRequest request) {
        return handle(() -> SERVICE.getOrdersByCustomer(request == null ? null : request.getCustomerId()));
    }

    private static <T> T handle(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (InvalidOrderException e) {
            throw new FunctionError("INVALID_REQUEST", e.getMessage());
        } catch (OrderNotFoundException e) {
            throw new FunctionError("ORDER_NOT_FOUND", e.getMessage());
        } catch (RuntimeException e) {
            // Log the real cause (CloudWatch), but never expose it to the client.
            e.printStackTrace();
            throw new FunctionError("INTERNAL_ERROR", "Unexpected error");
        }
    }
}