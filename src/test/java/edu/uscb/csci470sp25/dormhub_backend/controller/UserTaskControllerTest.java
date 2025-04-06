package edu.uscb.csci470sp25.dormhub_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserTaskControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(UserTaskControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Long testUserId;
    private Long testTaskId;
    private Long testUserTaskId;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Create a test user first.
        String newUserJson = "{\"username\":\"johndoe\",\"name\":\"John Doe\",\"email\":\"johndoe@example.com\"}";
        String userResponse = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer uid = JsonPath.read(userResponse, "$.id");
        testUserId = uid.longValue();

        // Create a test task.
        String newTaskJson = "{\"name\":\"Test Task\"}";
        String taskResponse = mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newTaskJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer tid = JsonPath.read(taskResponse, "$.id");
        testTaskId = tid.longValue();

        // Now create a user-task assignment.
        // Provide nested JSON objects for the associated user and task.
        String newUserTaskJson = "{\"deadline\":\"2025-05-01\",\"status\":false,"
                + "\"user\":{\"id\":" + testUserId + "},"
                + "\"task\":{\"id\":" + testTaskId + "}}";
        String userTaskResponse = mockMvc.perform(post("/usertask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserTaskJson))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Integer utid = JsonPath.read(userTaskResponse, "$.id");
        testUserTaskId = utid.longValue();

        logger.info("Setup complete. Test user ID: {}, Test task ID: {}, Test userTask ID: {}",
                testUserId, testTaskId, testUserTaskId);
    }

    @Test
    public void testGetUserTaskById() throws Exception {
        logger.info("Testing getUserTaskById with ID: {}", testUserTaskId);

        mockMvc.perform(get("/usertask/{id}", testUserTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserTaskId))
                // Verify that the nested user and task IDs are returned.
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

        mockMvc.perform(post("/usertask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deadline").value("2025-06-01"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.user.id").value(testUserId))
                .andExpect(jsonPath("$.task.id").value(testTaskId));

        logger.info("testCreateUserTask passed.");
    }

    @Test
    public void testGetAllUserTasks() throws Exception {
        logger.info("Testing getAllUserTasks");

        mockMvc.perform(get("/usertasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        logger.info("testGetAllUserTasks passed.");
    }

    @Test
    public void testUpdateUserTask() throws Exception {
        // Prepare updated JSON payload. Here we update deadline and status.
        String updatedUserTaskJson = "{\"deadline\":\"2025-07-01\",\"status\":true}";
        logger.info("Testing updateUserTask with ID: {} and payload: {}",
                testUserTaskId, updatedUserTaskJson);

        // Note: Depending on your implementation,
        // you might choose to allow updating of only selectable fields.
        mockMvc.perform(put("/usertask/{id}", testUserTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deadline").value("2025-07-01"))
                .andExpect(jsonPath("$.status").value(true));

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