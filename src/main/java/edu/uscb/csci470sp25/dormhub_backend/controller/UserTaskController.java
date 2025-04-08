package edu.uscb.csci470sp25.dormhub_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.uscb.csci470sp25.dormhub_backend.exception.UserTaskNotFoundException;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserTaskRepository;


@RestController
public class UserTaskController {

    @Autowired
    private UserTaskRepository userTaskRepository;

    
    // Create a new user-task assignment
    @PostMapping("/usertask")
    public UserTask newUserTask(@RequestBody UserTask newUserTask) {
        return userTaskRepository.save(newUserTask);
    }

    @GetMapping("/usertasks")
    public List<UserTask> getAllUserTasks(@RequestParam(required = false) Boolean status) {
        if (status != null) {
            return userTaskRepository.findByStatus(status, Sort.by(Sort.Direction.ASC, "id"));
        } else {
            return userTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
    }

    // Retrieve a specific user-task assignment by id
    @GetMapping("/usertask/{id}")
    public UserTask getUserTaskById(@PathVariable Long id) {
        return userTaskRepository.findById(id)
                .orElseThrow(() -> new UserTaskNotFoundException(id));
    }

    // Update an existing user-task assignment
    @PutMapping("/usertask/{id}")
    public UserTask updateUserTask(@RequestBody UserTask updatedUserTask, @PathVariable Long id, @AuthenticationPrincipal User user) {
        return userTaskRepository.findById(id)
                .map(userTask -> {
                	
                	if (user.getRole().equals("ADMIN")) {
                		userTask.setDeadline(updatedUserTask.getDeadline());
                	}
                	
                    // Update fields that represent the association's details
                    userTask.setStatus(updatedUserTask.isStatus());
                    // You might also allow updating the associated user or task if needed
                    return userTaskRepository.save(userTask);
                })
                .orElseThrow(() -> new UserTaskNotFoundException(id));
    }

    // Delete a user-task assignment
    @DeleteMapping("/usertask/{id}")
    public String deleteUserTask(@PathVariable Long id) {
        if (!userTaskRepository.existsById(id)) {
            throw new UserTaskNotFoundException(id);
        }
        userTaskRepository.deleteById(id);
        return "UserTask with id " + id + " has been deleted successfully.";
    }
}