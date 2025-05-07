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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import edu.uscb.csci470sp25.dormhub_backend.model.Task;
import edu.uscb.csci470sp25.dormhub_backend.model.User;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;
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
    private UserRepository userRepository;
    
    @Autowired 
    private TaskRepository taskRepository;
    
    @Autowired 
    private UserTaskRepository userTaskRepository;

    private Long testUserId;
    private Long testTaskId;
    private Long adminUserId;
    private Long testUserTaskId;
    private User testUser;
    private User adminUser;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setUsername("admin");
        adminUser.setEmail("adminuser@example.com");
        adminUser.setRole("ADMIN");
        adminUser = userRepository.save(adminUser);
        adminUserId = adminUser.getId();

        testUser = new User();
        testUser.setName("John Doe");
        testUser.setUsername("crouton");
        testUser.setEmail("crouton@example.com");
        testUser.setRole("PRIVILEGED_USER");
        testUser = userRepository.save(testUser);
        testUserId = testUser.getId();

        Task task = new Task();
        task.setName("Test Task");
        task = taskRepository.save(task);
        testTaskId = task.getId();

        UserTask userTask = new UserTask();
        userTask.setUser(testUser);
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
                    List.of(new SimpleGrantedAuthority(user.getRole()))
                )
            );
    }

	 @Test
	 public void testAdminCanGetAllUserTasks() throws Exception {
	        logger.info("Testing getAllUserTasks");
	        authenticateAs(adminUser);

	        MvcResult result = mockMvc.perform(get("/usertasks"))
	                .andDo(print())
	                .andExpect(status().isOk())
	                .andReturn();

	        System.out.println("Response content: " + result.getResponse().getContentAsString());
	        logger.info("testGetAllUserTasks passed.");
	 }

    @Test
    public void testAdminCanGetUserTaskById() throws Exception {
        authenticateAs(adminUser);

        mockMvc.perform(get("/usertask/{id}", testUserTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserTaskId));
    }

    @Test
    public void testPrivilegedUserCanGetOwnUserTaskById() throws Exception {
        authenticateAs(testUser);

        mockMvc.perform(get("/usertask/{id}", testUserTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserTaskId));
    }

    @Test
    public void testPrivilegedUserCannotAccessOthersTasks() throws Exception {
        authenticateAs(testUser);

        // Try accessing another user's task
        mockMvc.perform(get("/usertask/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAdminCanCreateUserTask() throws Exception {
        logger.info("Testing testAdminCanCreateUserTask");
        authenticateAs(adminUser);

        String json = String.format("""
            {
                "deadline":"2025-06-01",
                "status":true,
                "user":{"id":%d},
                "task":{"id":%d}
            }
        """, testUserId, testTaskId);

        mockMvc.perform(post("/usertask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists());

        logger.info("testAdminCanCreateUserTask passed.");
    }

    @Test
    public void testPrivilegedUserCanUpdateStatusOnly() throws Exception {
        authenticateAs(testUser);

        String json = """
            {
                "status":true
            }
        """;

        mockMvc.perform(put("/usertask/{id}", testUserTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    public void testAdminCanUpdateUserTaskFully() throws Exception {
        authenticateAs(adminUser);

        String json = String.format("""
            {
                "deadline":"2025-05-05",
                "status":true,
                "user":{"id":%d},
                "task":{"id":%d}
            }
        """, testUserId, testTaskId);

        mockMvc.perform(put("/usertask/{id}", testUserTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deadline").value("2025-05-05"))
            .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    public void testAdminCanDeleteUserTask() throws Exception {
        authenticateAs(adminUser);

        mockMvc.perform(delete("/usertask/{id}", testUserTaskId))
            .andExpect(status().isOk())
            .andExpect(content().string("UserTask with id " + testUserTaskId + " has been deleted successfully."));
    }
}
