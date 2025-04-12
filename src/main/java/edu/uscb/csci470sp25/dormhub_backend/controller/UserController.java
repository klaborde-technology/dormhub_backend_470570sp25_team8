package edu.uscb.csci470sp25.dormhub_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.uscb.csci470sp25.dormhub_backend.exception.UserNotFoundException;
import edu.uscb.csci470sp25.dormhub_backend.model.AppUser;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.repository.AppUserRepository;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AppUserRepository appUserRepository;


    // Create a new user
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/user")
    public User newUser(@RequestBody User newUser) {
        // Fetch the AppUser by ID (from the app_users table)
        AppUser appUser = appUserRepository.findById(newUser.getAppUser().getId())
                .orElseThrow(() -> new UserNotFoundException(newUser.getAppUser().getId()));
        
        // Set the appUser in the newUser object
        newUser.setAppUser(appUser);
        
        // Save the new user
        return userRepository.save(newUser);
    }

    // Retrieve all users sorted by id in ascending order
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    // Retrieve a specific user by id
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/user/{id}")
    public User getUserById(@PathVariable("id") final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Update a user with a given id
    @PutMapping("/user/{id}")
    public User updateUser(@RequestBody User newUser, @PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(newUser.getUsername());
                    user.setName(newUser.getName());
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