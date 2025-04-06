package edu.uscb.csci470sp25.dormhub_backend.exception;

public class UserTaskNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 582237545785868422L;

    public UserTaskNotFoundException(Long id) {
        super("Could not find the user-task with id " + id);
    }

    // added for unit tests
    public UserTaskNotFoundException(String message) {
        super(message);
    }
}