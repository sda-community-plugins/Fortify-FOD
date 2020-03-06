package com.serena.air.plugin.fod.models

public class GenericErrorResponse {
    private List<ErrorResponse> errors;

    public List<ErrorResponse> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        String result = "\n";
        for (ErrorResponse error : errors) {
            int errorNumber = errors.indexOf(error) + 1;
            result += errorNumber + ") " + error.getMessage() + (errorNumber > 1 ? "\n" : "");
        }
        return result;
    }
}


