package com.colorseq.abrowse.response;

public class ErrorResponse implements ServerResponse {

    private boolean error = true;

    private String message;

    public ErrorResponse() {

    }

    public ErrorResponse(String message) {

        this.message = message;
    }

    @Override
    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
