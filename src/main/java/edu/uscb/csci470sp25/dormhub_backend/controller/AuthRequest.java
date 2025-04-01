package edu.uscb.csci470sp25.dormhub_backend.controller;

public class AuthRequest {
    private String email;
    private String password;
    private String role;

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}