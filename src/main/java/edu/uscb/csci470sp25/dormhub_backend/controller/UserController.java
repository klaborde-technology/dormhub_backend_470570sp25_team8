package edu.uscb.csci470sp25.dormhub_backend.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import edu.uscb.csci470sp25.dormhub_backend.exception.UserNotFoundException;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;
 
@RestController
public class UserController {
 
    @Autowired
    private UserRepository userRepository;
 
    // Create a new user
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/user")
    public User newUser(@RequestBody User newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        return userRepository.save(newUser);
    }
 
    // Retrieve all users sorted by id and role in ascending order
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
    	return userRepository.findByRoleOrderByIdAsc("PRIVILEGED_USER");
    }
 
    // Retrieve a specific user by id
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") final Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!"PRIVILEGED_USER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Access denied: not a PRIVILEGED USER");
        }
        return user;
    }
 
    // Update a user with a given id
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/user/{id}")
    public User updateUser(@RequestBody User newUser, @PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(newUser.getUsername());
                    user.setName(newUser.getName());
                    user.setEmail(newUser.getEmail());
                    user.setPassword(newUser.getPassword());
                    user.setRole(newUser.getRole());
                    return userRepository.save(user);
                }).orElseThrow(() -> new UserNotFoundException(id));

    }
 
    // Delete a user by id
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);

        }
        userRepository.deleteById(id);
        return "User with id " + id + " has been deleted successfully.";

    }

}
 