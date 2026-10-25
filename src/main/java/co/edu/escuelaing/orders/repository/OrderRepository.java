package co.edu.escuelaing.orders.repository;

import co.edu.escuelaing.orders.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    String nextId();

    Order save(Order order);

    Optional<Order> findById(String id);

    List<Order> findByCustomerId(String customerId);

    /** Removes the order. Returns true if it existed. */
    boolean deleteById(String id);
}