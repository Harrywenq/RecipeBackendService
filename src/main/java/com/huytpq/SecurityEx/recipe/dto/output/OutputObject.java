package com.huytpq.SecurityEx.recipe.dto.output;

import org.springframework.http.HttpStatus;

public class OutputObject {
    private String message;
    private HttpStatus status;
    private Object data;

    public OutputObject() {
    }

    public OutputObject(String message, HttpStatus status, Object data) {
        this.message = message;
        this.status = status;
        this.data = data;
    }

    // Getter & Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
