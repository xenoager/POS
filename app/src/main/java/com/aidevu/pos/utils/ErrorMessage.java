package com.aidevu.pos.utils;

public class ErrorMessage {

    private String errorMessage;
    private double time;

    public ErrorMessage(String errorMessage, double time) {
        this.errorMessage = errorMessage;
        this.time = time;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public double getTime() {
        return time;
    }
}