package com.example.ogani.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOSResponse<T> {
    private Integer error;
    private String message;
    private T data;

    public static <T> PayOSResponse<T> success(T data) {
        return new PayOSResponse<>(0, "success", data);
    }

    public static <T> PayOSResponse<T> success(String message, T data) {
        return new PayOSResponse<>(0, message, data);
    }

    public static <T> PayOSResponse<T> error(String message) {
        return new PayOSResponse<>(-1, message, null);
    }

    public static <T> PayOSResponse<T> error(int code, String message) {
        return new PayOSResponse<>(code, message, null);
    }
}