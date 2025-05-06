package edu.uscb.csci470sp25.dormhub_backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.uscb.csci470sp25.dormhub_backend.model.Task;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserTaskRepositoryTest {

    @Autowired
    private UserTaskRepository userTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testFindById() {
        // Arrange: create and save a valid User and Task
        User user = new User();
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("testuser@example.com");
        user = userRepository.save(user);

        Task task = new Task();
        task.setName("Test Task");
        task = taskRepository.save(task);

        // Arrange: create and save a valid UserTask
        UserTask userTask = new UserTask();
        userTask.setDeadline(LocalDate.of(2025, 5, 1));
        userTask.setStatus(false);
        userTask.setUser(user);
        userTask.setTask(task);
        userTask = userTaskRepository.save(userTask);

        // Act: retrieve the UserTask by id
        Optional<UserTask> foundUserTask = userTaskRepository.findById(userTask.getId());

        // Assert
        assertTrue(foundUserTask.isPresent());
        assertEquals(LocalDate.of(2025, 5, 1), foundUserTask.get().getDeadline());
        assertFalse(foundUserTask.get().isStatus());
        assertEquals(user.getId(), foundUserTask.get().getUser().getId());
        assertEquals(task.getId(), foundUserTask.get().getTask().getId());
    }

    @Test
    public void testSave() {
        // Arrange: create valid User and Task
        User user = new User();
        user.setUsername("saveuser");
        user.setName("Save Test User");
        user.setEmail("saveduser@example.com");
        user = userRepository.save(user);

        Task task = new Task();
        task.setName("Save Test Task");
        task = taskRepository.save(task);

        // Arrange: create and save a UserTask
        UserTask userTask = new UserTask();
        userTask.setDeadline(LocalDate.of(2025, 6, 1));
        userTask.setStatus(true);
        userTask.setUser(user);
        userTask.setTask(task);
        UserTask savedUserTask = userTaskRepository.save(userTask);

        // Assert
        assertEquals(LocalDate.of(2025, 6, 1), savedUserTask.getDeadline());
        assertTrue(savedUserTask.isStatus());
        assertTrue(userTaskRepository.findById(savedUserTask.getId()).isPresent());
    }

    @Test
    public void testDeleteById() {
        // Arrange: create valid User and Task
        User user = new User();
        user.setUsername("deleteuser");
        user.setName("Delete User");
        user.setEmail("deleteuser@example.com");
        user = userRepository.save(user);

        Task task = new Task();
        task.setName("Delete Task");
        task = taskRepository.save(task);

        // Arrange: create and save a UserTask
        UserTask userTask = new UserTask();
        userTask.setDeadline(LocalDate.of(2025, 7, 1));
        userTask.setStatus(false);
        userTask.setUser(user);
        userTask.setTask(task);
        userTask = userTaskRepository.save(userTask);
        Long userTaskId = userTask.getId();

        // Act: delete the UserTask by id
        userTaskRepository.deleteById(userTaskId);

        // Assert
        assertFalse(userTaskRepository.findById(userTaskId).isPresent());
    }
}