package co.edu.escuelaing.orders.lambda;

/** Lambda input for operations that need an order id: {"id": "ORD-001"} */
public class OrderIdRequest {

    private String id;

    public OrderIdRequest() {
    }

    public OrderIdRequest(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}