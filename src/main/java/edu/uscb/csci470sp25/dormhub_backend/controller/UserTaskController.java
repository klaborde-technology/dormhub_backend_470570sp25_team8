package edu.uscb.csci470sp25.dormhub_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserTaskRepository;

@RestController
public class UserTaskController {

    @Autowired
    private UserTaskRepository userTaskRepository;

    // Create a new user-task assignment
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/usertask")
    public UserTask newUserTask(@RequestBody UserTask newUserTask) {
        return userTaskRepository.save(newUserTask);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/usertasks")
    public List<UserTask> getAllUserTasks(
            @RequestParam(required = false) Boolean status,
            @AuthenticationPrincipal User user) {

        String role = user.getAppUser().getRole();

        if (role.equals("ADMIN")) {
            if (status != null) {
                return userTaskRepository.findByStatus(status, Sort.by(Sort.Direction.ASC, "id"));
            } else {
                return userTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
            }
        }

        // PRIVILEGED_USER path
        if (status != null) {
            return userTaskRepository.findByUserIdAndStatus(status, user.getId(), Sort.by(Sort.Direction.ASC, "id"));
        } else {
            return userTaskRepository.findByUserId(user.getId(), Sort.by(Sort.Direction.ASC, "id"));
        }
    }


    // Retrieve a specific user-task assignment by id
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRIVILEGED_USER')")
    @GetMapping("/usertask/{id}")
    public UserTask getUserTaskById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return userTaskRepository.findById(id)
            .map(userTask -> {
                boolean isAdmin = currentUser.getAppUser().getRole().equals("ADMIN");
                boolean isOwner = userTask.getUser().getId().equals(currentUser.getId());

                if (!isAdmin && !isOwner) {
                    throw new AccessDeniedException("You can only view your own tasks.");
                }

                return userTask;
            })
            .orElseThrow(() -> new UserTaskNotFoundException(id));
    }


    // Update an existing user-task assignment
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRIVILEGED_USER')")
    @PutMapping("/usertask/{id}")
    public UserTask updateUserTask(@RequestBody UserTask updatedUserTask, @PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return userTaskRepository.findById(id)
            .map(userTask -> {
                boolean isAdmin = currentUser.getAppUser().getRole().equals("ADMIN");
                boolean isOwner = userTask.getUser().getId().equals(currentUser.getId());

                if (!isAdmin && !isOwner) {
                    throw new AccessDeniedException("You can only update your own tasks.");
                }

                if (isAdmin) {
                    // Admins can update all fields
                    userTask.setDeadline(updatedUserTask.getDeadline());
                    userTask.setStatus(updatedUserTask.isStatus());
                    // you could allow changing task/user here if needed
                } else {
                    // PRIVILEGED users can only change the status
                    userTask.setStatus(updatedUserTask.isStatus());
                }

                return userTaskRepository.save(userTask);
            })
            .orElseThrow(() -> new UserTaskNotFoundException(id));
    }


    // Delete a user-task assignment
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/usertask/{id}")
    public String deleteUserTask(@PathVariable Long id) {
        if (!userTaskRepository.existsById(id)) {
            throw new UserTaskNotFoundException(id);
        }
        userTaskRepository.deleteById(id);
        return "UserTask with id " + id + " has been deleted successfully.";
    }
}