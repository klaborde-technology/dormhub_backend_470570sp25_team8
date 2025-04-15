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

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/usertasks")
    public List<UserTask> getAllUserTasks(@RequestParam(required = false) Boolean status) {

        if (status != null) {
            return userTaskRepository.findByStatus(status, Sort.by(Sort.Direction.ASC, "id"));
        } else {
            return userTaskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
    }
    
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/usertask/{id}")
    public UserTask getUserTaskByIdForAdmin(@PathVariable Long id) {
        return userTaskRepository.findById(id)
                .orElseThrow(() -> new UserTaskNotFoundException(id));
    }
    
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
    
    @PreAuthorize("hasAuthority('PRIVILEGED_USER')")
    @GetMapping("/usertasks/{id}")
    public List<UserTask> getTasksForPrivilegedUser(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own tasks.");
        }
        try {
            return userTaskRepository.findByUserId(userId, Sort.by(Sort.Direction.ASC, "id"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied", e);
        }
    }

    // === BOTH ROLES ===
    
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
                        userTask.setUser(updatedUserTask.getUser());
                        userTask.setTask(updatedUserTask.getTask());
                    } else {
                        userTask.setStatus(updatedUserTask.isStatus());
                    }

                    return userTaskRepository.save(userTask);
                })
                .orElseThrow(() -> new UserTaskNotFoundException(id));
    }

}