package utils.Responses;

import utils.Requests.RequestType;

public class Response<T> {

    private final ResponseStatus status;
    private final RequestType type;
    private final String message;
    private final T data;

    private Response(ResponseStatus status, RequestType type, String message, T data) {
        this.status = status;
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(RequestType type, T data) {
        return new Response<>(ResponseStatus.SUCCESS, type, null, data);
    }

    public static <T> Response<T> error(RequestType type, String message) {
        return new Response<>(ResponseStatus.ERROR, type, message, null);
    }

    // Add other utility methods, getters, etc.

    public ResponseStatus getStatus() {
        return status;
    }

    public RequestType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
