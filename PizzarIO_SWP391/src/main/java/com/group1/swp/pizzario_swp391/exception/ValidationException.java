package com.group1.swp.pizzario_swp391.exception;

import java.util.Map;

import lombok.Getter;


@Getter
public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = fieldErrors;
    }


    public boolean hasErrors() {
        return !fieldErrors.isEmpty();
    }

}
