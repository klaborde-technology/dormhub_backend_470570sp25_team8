package edu.uscb.csci470sp25.dormhub_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import edu.uscb.csci470sp25.dormhub_backend.exception.UserTaskNotFoundException;
import edu.uscb.csci470sp25.dormhub_backend.model.Task;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;
import edu.uscb.csci470sp25.dormhub_backend.repository.TaskRepository;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserTaskRepository;

@RestController
public class UserTaskController {

    @Autowired
    private UserTaskRepository userTaskRepository;
    
    @Autowired 
    private TaskRepository taskRepository;
    
    @Autowired 
    private UserRepository userRepository;

    // === ADMIN ONLY ===
    
    // Create a new user-task assignment
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/usertask")
    public UserTask newUserTask(@RequestBody UserTask newUserTask) {
        Long userId = newUserTask.getUser().getId();
        Long taskId = newUserTask.getTask().getId();

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found"));

        newUserTask.setUser(user);
        newUserTask.setTask(task);

        return userTaskRepository.save(newUserTask);
    }

    // Retrieves all user tasks, with optional filtering by status, accessible only to ADMINs.
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/usertasks")
    public List<UserTask> getAllUserTasks(@RequestParam(required = false) Boolean status) {
        if (status != null) {
            return userTaskRepository.findByStatus(status, Sort.by(Sort.Direction.ASC, "id"));
        } else {
            return userTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
    }
    
    // Deletes a user task by ID, allowing access only to ADMINs.
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/usertask/{id}")
    public String deleteUserTask(@PathVariable Long id) {
        if (!userTaskRepository.existsById(id)) {
            throw new UserTaskNotFoundException(id);
        }
        userTaskRepository.deleteById(id);
        return "UserTask with id " + id + " has been deleted successfully.";
    }
    
    // === PRIVILEGED_USER ONLY ===
    
    // Retrieves tasks for a PRIVILEGED_USER, allowing access only to their own tasks.
    @PreAuthorize("hasAuthority('PRIVILEGED_USER')")
    @GetMapping("/usertasks/user/{id}")
    public List<UserTask> getTasksForPrivilegedUser(
    		@PathVariable("id") Long userId,
    		@RequestParam(required = false) Boolean status,
    		@AuthenticationPrincipal User currentUser) {
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own tasks.");
        }
        try {
        	if (status != null) {
        		return userTaskRepository.findByUserIdAndStatus(userId, status, Sort.by(Sort.Direction.ASC, "id"));     	
        	} else {
        		return userTaskRepository.findByUserId(userId, Sort.by(Sort.Direction.ASC, "id"));
        	}
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied", e);
        }
    }

    // === BOTH ROLES ===
    
    // Retrieves a user task by ID, allowing access only to ADMINs or the task owner.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRIVILEGED_USER')")
    @GetMapping("/usertask/{id}")
    public UserTask getUserTaskById(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        UserTask userTask = userTaskRepository.findById(id)
                .orElseThrow(() -> new UserTaskNotFoundException(id));
        // Allow access if ADMIN or the user is the owner of the task
        boolean isAdmin = currentUser.getRole().equals("ADMIN");
        boolean isOwner = userTask.getUser().getId().equals(currentUser.getId());
        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this task.");
        }
        return userTask;
    }
    
    // Updates a user task, allowing changes only for ADMINs or the task owner.
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PRIVILEGED_USER')")
    @PutMapping("/usertask/{id}")
    public UserTask updateUserTask(@RequestBody UserTask updatedUserTask,
                                   @PathVariable Long id,
                                   @AuthenticationPrincipal User currentUser) {

        return userTaskRepository.findById(id)
                .map(userTask -> {
                    boolean isAdmin = currentUser.getRole().equals("ADMIN");
                    boolean isOwner = userTask.getUser().getId().equals(currentUser.getId());

                    if (!isAdmin && !isOwner) {
                        throw new AccessDeniedException("You can only update your own tasks.");
                    }

                    if (isAdmin) {
                        userTask.setDeadline(updatedUserTask.getDeadline());
                        userTask.setStatus(updatedUserTask.isStatus());
                    } else {
                        userTask.setStatus(updatedUserTask.isStatus());
                    }

                    return userTaskRepository.save(userTask);
                })
                .orElseThrow(() -> new UserTaskNotFoundException(id));
    }

}