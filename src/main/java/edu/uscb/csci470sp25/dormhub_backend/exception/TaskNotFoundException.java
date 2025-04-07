package edu.uscb.csci470sp25.dormhub_backend.exception;

public class TaskNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 582237545785868421L;

    public TaskNotFoundException(Long id) {
        super("Could not find the task with id " + id);
    }

    // Added for unit testing
    public TaskNotFoundException(String message) {
        super(message);
    }
}