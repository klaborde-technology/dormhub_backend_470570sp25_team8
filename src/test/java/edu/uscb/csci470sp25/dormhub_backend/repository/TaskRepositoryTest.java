package edu.uscb.csci470sp25.dormhub_backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.uscb.csci470sp25.dormhub_backend.model.Task;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testFindById() {
        // Arrange: create and save a valid Task
        Task task = new Task();
        task.setName("Test Task");
        task = taskRepository.save(task);

        // Act: retrieve the task
        Optional<Task> foundTask = taskRepository.findById(task.getId());

        // Assert
        assertTrue(foundTask.isPresent());
        assertEquals("Test Task", foundTask.get().getName());
    }

    @Test
    public void testSave() {
        // Arrange: create a valid task
        Task task = new Task();
        task.setName("Another Task");

        // Act: save the task
        Task savedTask = taskRepository.save(task);

        // Assert
        assertEquals("Another Task", savedTask.getName());
        assertTrue(taskRepository.findById(savedTask.getId()).isPresent());
    }

    @Test
    public void testDeleteById() {
        // Arrange: create and save a task
        Task task = new Task();
        task.setName("Task To Delete");
        task = taskRepository.save(task);
        Long taskId = task.getId();

        // Act: delete the task by Id
        taskRepository.deleteById(taskId);

        // Assert
        assertFalse(taskRepository.findById(taskId).isPresent());
    }
}