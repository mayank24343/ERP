package edu.univ.erp.api.common;

public class ApiResult<T> {
    private final boolean success;
    private final String message;
    private final T data;


    //Constructor, this is private to ensure only certain object types can be created, to avoid things like success: false, data: some value
    private ApiResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    //Following methods are static, we don't need ApiResult Instance to create objects
    //<T> is generic type parameter because results can have different data types

    //Api returns only data
    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(true, null, data);
    }

    //Api returns only message
    public static <T> ApiResult<T> okMessage(String msg) {
        return new ApiResult<>(true, msg, null);
    }

    //Api error
    public static <T> ApiResult<T> error(String msg) {
        return new ApiResult<>(false, msg, null);
    }

    //getter methods
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
