package co.edu.escuelaing.orders.lambda;

/** Lambda input for the customer search: {"customerId": "CUST-42"} */
public class CustomerOrdersRequest {

    private String customerId;

    public CustomerOrdersRequest() {
    }

    public CustomerOrdersRequest(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}