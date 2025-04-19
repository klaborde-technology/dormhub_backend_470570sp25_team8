package edu.uscb.csci470sp25.dormhub_backend.service;

import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;
import edu.uscb.csci470sp25.dormhub_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Registers a new user by encoding their password and saving them to the database.
     */
    public String registerUser(String name, String username, String email, String role, String password) {
        // ✅ Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists.");
        }

        // ✅ Encode password before saving user
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User();
        newUser.setName(name);
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setPassword(hashedPassword);
        
        
        userRepository.save(newUser);

        return "User registered successfully";
    }

    /**
     * Authenticates a user by verifying their email and password, then generating a JWT token.
     */
    public String authenticateUser(String email, String password) {
        // ✅ Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password.");
        }

        User user = userOptional.get();

        // ✅ Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        // ✅ Generate JWT token using JwtUtil
        return jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
    }
}