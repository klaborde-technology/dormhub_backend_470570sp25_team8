package edu.uscb.csci470sp25.dormhub_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import edu.uscb.csci470sp25.dormhub_backend.exception.TaskNotFoundException;
import edu.uscb.csci470sp25.dormhub_backend.model.Task;
import edu.uscb.csci470sp25.dormhub_backend.repository.TaskRepository;

@RestController
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    // Create a new task
    @PostMapping("/task")
    public Task newTask(@RequestBody Task newTask) {
        return taskRepository.save(newTask);
    }

    // Retrieve all tasks sorted by id in ascending order
    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    // Retrieve a specific task by id
    @GetMapping("/task/{id}")
    public Task getTaskById(@PathVariable("id") final Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    // Update a task by id
    @PutMapping("/task/{id}")
    public Task updateTask(@RequestBody Task updatedTask, @PathVariable Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    task.setName(updatedTask.getName());
                    return taskRepository.save(task);
                }).orElseThrow(() -> new TaskNotFoundException(id));
    }

    // Delete a task by id
    @DeleteMapping("/task/{id}")
    public String deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
        return "Task with id " + id + " has been deleted successfully.";
    }
}