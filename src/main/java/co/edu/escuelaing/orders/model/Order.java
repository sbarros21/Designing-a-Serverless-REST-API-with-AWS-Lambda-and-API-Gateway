package co.edu.escuelaing.orders.model;

import java.util.Objects;

public class Order {

    private String id;
    private String customerId;
    private String product;
    private Integer quantity;

    public Order() {
    }

    public Order(String id, String customerId, String product, Integer quantity) {
        this.id = id;
        this.customerId = customerId;
        this.product = product;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getProduct() { return product; }
    public void setProduct(String product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order other = (Order) o;
        return Objects.equals(id, other.id)
                && Objects.equals(customerId, other.customerId)
                && Objects.equals(product, other.product)
                && Objects.equals(quantity, other.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customerId, product, quantity);
    }

    @Override
    public String toString() {
        return "Order{id='" + id + "', customerId='" + customerId
                + "', product='" + product + "', quantity=" + quantity + "}";
    }
}