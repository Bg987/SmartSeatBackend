package com.example.SmartSeatBackend.utility;




public class ApiResponse {
    private Boolean success;
    private String message;
    private Object data;

    public ApiResponse(Boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}

