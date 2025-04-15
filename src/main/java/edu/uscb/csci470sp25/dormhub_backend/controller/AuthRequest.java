package edu.uscb.csci470sp25.dormhub_backend.controller;

public class AuthRequest {
    private String email;
    private String password;
    private String role;
    private String username;
    private String name;

    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getPassword() { return password; }
    
}