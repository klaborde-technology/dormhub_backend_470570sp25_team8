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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TaskControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Long testTaskId;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        String newTaskJson = "{\"name\":\"Test Task\"}";
        String response = mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newTaskJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer id = JsonPath.read(response, "$.id");
        testTaskId = id.longValue();

        logger.info("Setup complete. Test task ID: {}", testTaskId);
    }

    @Test
    public void testGetTaskById() throws Exception {
        logger.info("Testing getTaskById with ID: {}", testTaskId);

        mockMvc.perform(get("/task/{id}", testTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTaskId))
                .andExpect(jsonPath("$.name").value("Test Task"));

        logger.info("testGetTaskById passed.");
    }

    @Test
    public void testCreateTask() throws Exception {
        String newTaskJson = "{\"name\":\"New Task\"}";
        logger.info("Testing createTask with payload: {}", newTaskJson);

        mockMvc.perform(post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Task"));

        logger.info("testCreateTask passed.");
    }

    @Test
    public void testGetAllTasks() throws Exception {
        logger.info("Testing getAllTasks");

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        logger.info("testGetAllTasks passed.");
    }

    @Test
    public void testUpdateTask() throws Exception {
        String updatedTaskJson = "{\"name\":\"Updated Task\"}";
        logger.info("Testing updateTask with ID: {} and payload: {}", testTaskId, updatedTaskJson);

        mockMvc.perform(put("/task/{id}", testTaskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedTaskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Task"));

        logger.info("testUpdateTask passed.");
    }

    @Test
    public void testDeleteTask() throws Exception {
        logger.info("Testing deleteTask with ID: {}", testTaskId);

        mockMvc.perform(delete("/task/{id}", testTaskId))
                .andExpect(status().isOk())
                .andExpect(content().string("Task with id " + testTaskId + " has been deleted successfully."));

        logger.info("testDeleteTask passed.");
    }
}