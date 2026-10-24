package co.edu.escuelaing.orders.repository;

import co.edu.escuelaing.orders.model.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * In-memory storage. NOT persistent: data is lost whenever the
 * Lambda execution environment is recycled.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> orders = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    public static InMemoryOrderRepository withSampleData() {
        InMemoryOrderRepository repository = new InMemoryOrderRepository();
        repository.save(new Order(repository.nextId(), "CUST-42", "MacBook Air", 1));
        repository.save(new Order(repository.nextId(), "CUST-42", "Mouse", 2));
        repository.save(new Order(repository.nextId(), "CUST-7", "Keyboard", 2));
        return repository;
    }

    @Override
    public String nextId() {
        return String.format("ORD-%03d", sequence.incrementAndGet());
    }

    @Override
    public Order save(Order order) {
        orders.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }
}