package co.edu.escuelaing.orders.lambda;

/**
 * Error thrown to the Lambda runtime. Its message starts with a contract
 * error code, e.g. "[ORDER_NOT_FOUND] Order ORD-999 does not exist", so that
 * API Gateway can select the HTTP status with a regex on errorMessage.
 */
public class FunctionError extends RuntimeException {

    private final String code;

    public FunctionError(String code, String message) {
        super("[" + code + "] " + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}