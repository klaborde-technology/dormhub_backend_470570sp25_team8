package edu.uscb.csci470sp25.dormhub_backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalNotFoundExceptionAdvice {

    @ResponseBody
    @ExceptionHandler({
        UserNotFoundException.class,
        TaskNotFoundException.class,
        UserTaskNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundExceptions(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("errorMessage", ex.getMessage());
        return errorResponse;
    }
}