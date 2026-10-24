package co.edu.escuelaing.orders.service;

import co.edu.escuelaing.orders.model.CreateOrderRequest;
import co.edu.escuelaing.orders.model.Order;
import co.edu.escuelaing.orders.repository.OrderRepository;

import java.util.List;
import java.util.Objects;

/**
 * Application logic for orders. Knows nothing about HTTP, API Gateway or AWS.
 */
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    public Order createOrder(CreateOrderRequest request) {
        validate(request);
        Order order = new Order(
                repository.nextId(),
                request.getCustomerId().trim(),
                request.getProduct().trim(),
                request.getQuantity());
        return repository.save(order);
    }

    public Order getOrder(String id) {
        if (isBlank(id)) {
            throw new InvalidOrderException("Order id is required");
        }
        return repository.findById(id.trim())
                .orElseThrow(() -> new OrderNotFoundException(id.trim()));
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        if (isBlank(customerId)) {
            throw new InvalidOrderException("customerId is required");
        }
        return repository.findByCustomerId(customerId.trim());
    }

    private void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new InvalidOrderException("Request body is required");
        }
        if (isBlank(request.getCustomerId())) {
            throw new InvalidOrderException("customerId is required");
        }
        if (isBlank(request.getProduct())) {
            throw new InvalidOrderException("product is required");
        }
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new InvalidOrderException("quantity must be an integer greater than or equal to 1");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}