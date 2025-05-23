package br.com.pointer.pointer_back.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String message;
    private int status;
    private T content;
    private List<String> errors;

    @JsonIgnore
    public boolean isOk() {
        return status == 200;
    }

    public ApiResponse<T> ok(T content, String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .content(content)
                .status(200)
                .build();
    }

    public ApiResponse<T> notFound(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(404)
                .build();
    }

    public ApiResponse<T> badRequest(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(400)
                .build();
    }

    public ApiResponse<T> badRequest(String message, List<String> errors) {
        return ApiResponse.<T>builder()
                .message(message)
                .errors(errors)
                .status(400)
                .build();
    }

    public ApiResponse<T> conflict(String message, T content) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(409)
                .content(content)
                .build();
    }

    public ApiResponse<T> unprocessableEntity(String message) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(422)
                .build();
    }
} 