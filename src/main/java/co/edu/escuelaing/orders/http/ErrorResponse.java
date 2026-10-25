package co.edu.escuelaing.orders.http;

/** Error body defined by the API contract: {"error": "...", "message": "..."} */
public class ErrorResponse {

    private final String error;
    private final String message;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public String getError() { return error; }
    public String getMessage() { return message; }
}