package edu.uscb.csci470sp25.dormhub_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import edu.uscb.csci470sp25.dormhub_backend.model.AppUser;
import edu.uscb.csci470sp25.dormhub_backend.model.Task;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;
import edu.uscb.csci470sp25.dormhub_backend.repository.AppUserRepository;
import edu.uscb.csci470sp25.dormhub_backend.repository.TaskRepository;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;
import edu.uscb.csci470sp25.dormhub_backend.repository.UserTaskRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserTaskControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskControllerTest.class);

    @Autowired 
    private MockMvc mockMvc;
    
    @Autowired 
    private WebApplicationContext webApplicationContext;
    
    @Autowired 
    private AppUserRepository appUserRepository;
    
    @Autowired 
    private UserRepository userRepository;
    
    @Autowired 
    private TaskRepository taskRepository;
    
    @Autowired 
    private UserTaskRepository userTaskRepository;

    private Long testUserId;
    private Long testTaskId;
    private Long testUserTaskId;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // AppUser
        AppUser appUser = new AppUser();
        appUser.setEmail("johndoe@example.com");
        appUser.setPassword("password1234");
        appUser.setRole("PRIVILEGED_USER");
        appUser = appUserRepository.save(appUser);

        // User
        User user = new User();
        user.setName("John Doe");
        user.setUsername("johndoe");
        user.setAppUser(appUser);
        user = userRepository.save(user);
        testUserId = user.getId();

        // Task
        Task task = new Task();
        task.setName("Test Task");
        task = taskRepository.save(task);
        testTaskId = task.getId();

        // UserTask
        UserTask userTask = new UserTask();
        userTask.setUser(user);
        userTask.setTask(task);
        userTask.setDeadline(LocalDate.of(2025, 5, 1));
        userTask.setStatus(false);
        userTask = userTaskRepository.save(userTask);
        testUserTaskId = userTask.getId();

        logger.info("Setup complete. User: {}, Task: {}, UserTask: {}", testUserId, testTaskId, testUserTaskId);
    }

    private void authenticateAs(User user) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority(user.getAppUser().getRole()))
            )
        );
    }

    @Test
    public void testGetUserTaskById() throws Exception {
        logger.info("Testing getUserTaskById with ID: {}", testUserTaskId);

        User currentUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        authenticateAs(currentUser);

        mockMvc.perform(get("/usertask/{id}", testUserTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserTaskId))
                .andExpect(jsonPath("$.user.id").value(testUserId))
                .andExpect(jsonPath("$.task.id").value(testTaskId));

        logger.info("testGetUserTaskById passed.");
    }



    @Test
    public void testCreateUserTask() throws Exception {
        // Create another user-task assignment.
        String newUserTaskJson = "{\"deadline\":\"2025-06-01\",\"status\":true,"
                + "\"user\":{\"id\":" + testUserId + "},"
                + "\"task\":{\"id\":" + testTaskId + "}}";

        logger.info("Testing createUserTask with payload: {}", newUserTaskJson);

        // Authenticate as the current user
        User currentUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        authenticateAs(currentUser);

        // Perform the POST request to create a new user-task assignment
        mockMvc.perform(post("/usertask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserTaskJson))
                .andExpect(status().isOk()) // Expect HTTP 200 (OK) status
                .andExpect(jsonPath("$.deadline").value("2025-06-01")) // Check that the deadline matches
                .andExpect(jsonPath("$.status").value(true)) // Verify that the status is true
                .andExpect(jsonPath("$.user.id").value(testUserId)) // Check that the user ID is correct
                .andExpect(jsonPath("$.task.id").value(testTaskId)) // Check that the task ID is correct
                // Optionally, check that the newly created UserTask has the correct ID
                .andExpect(jsonPath("$.id").exists());


        logger.info("testCreateUserTask passed.");
    }

    
    @Test
    public void testGetAllUserTasks() throws Exception {
        logger.info("Testing getAllUserTasks");

        // Fetch the admin user from the database
        AppUser adminAppUser = appUserRepository.findByEmail("johndoe@example.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        // Get the associated User object for the AppUser
        User adminUser = userRepository.findByAppUser(adminAppUser)
                .orElseThrow(() -> new RuntimeException("User not found for admin"));

        // Authenticate as the admin user by setting the Spring Security context
        authenticateAs(adminUser);

        // Perform the GET request for all user tasks
        mockMvc.perform(get("/usertasks"))
                .andExpect(status().isOk())  // Ensure the response status is 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // Expect JSON content type
                .andExpect(jsonPath("$").isArray())  // Ensure the response body is an array
                .andExpect(jsonPath("$[0].user.id").value(testUserId))  // Check the user ID of the first user task
                .andExpect(jsonPath("$[0].task.id").value(testTaskId)); // Check if the task ID is included

        logger.info("testGetAllUserTasks passed.");
    }


    @Test
    public void testUpdateUserTask() throws Exception {
        // Prepare updated JSON payload. Here we update deadline and status.
        String updatedUserTaskJson = "{\"deadline\":\"2025-05-01\",\"status\":true}";
        logger.info("Testing updateUserTask with ID: {} and payload: {}", testUserTaskId, updatedUserTaskJson);

        // Authenticate as an admin user (ensure the user has ADMIN role)
        User adminUser = userRepository.findById(testUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        authenticateAs(adminUser);  // Authenticate as an admin user

        // Perform the PUT request to update the UserTask
        mockMvc.perform(put("/usertask/{id}", testUserTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserTaskJson))
                .andExpect(status().isOk())  // Ensure HTTP 200 OK status
                .andDo(result -> System.out.println(result.getResponse().getContentAsString())) // Log the response body
                .andExpect(jsonPath("$.id").value(testUserTaskId))  // Verify that the ID remains unchanged
                .andExpect(jsonPath("$.deadline").value("2025-05-01"))  // Verify the updated deadline
                .andExpect(jsonPath("$.status").value(true));  // Verify the updated status


        logger.info("testUpdateUserTask passed.");
    }


    @Test
    public void testDeleteUserTask() throws Exception {
        logger.info("Testing deleteUserTask with ID: {}", testUserTaskId);

        mockMvc.perform(delete("/usertask/{id}", testUserTaskId))
                .andExpect(status().isOk())
                .andExpect(content().string("UserTask with id " + testUserTaskId + " has been deleted successfully."));

        logger.info("testDeleteUserTask passed.");
    }
}