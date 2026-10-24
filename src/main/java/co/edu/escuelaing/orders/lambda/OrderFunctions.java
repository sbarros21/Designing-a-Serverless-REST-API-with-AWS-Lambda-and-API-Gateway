package co.edu.escuelaing.orders.lambda;

import co.edu.escuelaing.orders.model.CreateOrderRequest;
import co.edu.escuelaing.orders.model.Order;
import co.edu.escuelaing.orders.repository.InMemoryOrderRepository;
import co.edu.escuelaing.orders.service.OrderService;

import java.util.List;

/**
 * Lambda entry points (Architecture A: non-proxy integration).
 * Plain Java: no AWS classes. The Lambda Java runtime converts the
 * incoming JSON into the parameter type and the return value back into JSON.
 */
public class OrderFunctions {

    // Created once per execution environment (cold start) and reused while it stays warm.
    private static final OrderService SERVICE =
            new OrderService(InMemoryOrderRepository.withSampleData());

    public Order createOrder(CreateOrderRequest request) {
        return SERVICE.createOrder(request);
    }

    public Order getOrder(OrderIdRequest request) {
        return SERVICE.getOrder(request == null ? null : request.getId());
    }

    public List<Order> findOrdersByCustomer(CustomerOrdersRequest request) {
        return SERVICE.getOrdersByCustomer(request == null ? null : request.getCustomerId());
    }
}
