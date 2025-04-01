package edu.uscb.csci470sp25.dormhub_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
public class UserControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private Long testUserId;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        // Insert a test user into the database and retrieve its ID
        String newUserJson = "{\"username\":\"johndoe\",\"name\":\"John Doe\",\"email\":\"johndoe@example.com\"}";
        String response = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the ID from the response (assuming the response contains the user ID)
        Integer id = JsonPath.read(response, "$.id");
        testUserId = id.longValue();

        logger.info("Setup complete. Test user ID: {}", testUserId);
    }

    @Test
    public void testGetUserById() throws Exception {
        // Arrange
        logger.info("Testing getUserById with ID: {}", testUserId);

        // Act & Assert
        mockMvc.perform(get("/user/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId));

        logger.info("testGetUserById passed.");
    }

    @Test
    public void testCreateUser() throws Exception {
        // Arrange
        String newUserJson = "{\"username\":\"johndoe\",\"name\":\"John Doe\",\"email\":\"johndoe@example.com\"}";
        logger.info("Testing createUser with payload: {}", newUserJson);

        // Act & Assert
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("johndoe@example.com"));

        logger.info("testCreateUser passed.");
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // Arrange
        logger.info("Testing getAllUsers");

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        logger.info("testGetAllUsers passed.");
    }

    @Test
    public void testUpdateUser() throws Exception {
        // Arrange
        String updatedUserJson = "{\"username\":\"janedoe\",\"name\":\"Jane Doe\",\"email\":\"janedoe@example.com\"}";
        logger.info("Testing updateUser with ID: {} and payload: {}", testUserId, updatedUserJson);

        // Act & Assert
        mockMvc.perform(put("/user/{id}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("janedoe@example.com"));

        logger.info("testUpdateUser passed.");
    }

    @Test
    public void testDeleteUser() throws Exception {
        // Arrange
        logger.info("Testing deleteUser with ID: {}", testUserId);

        // Act & Assert
        mockMvc.perform(delete("/user/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + testUserId + " has been deleted successfully."));

        logger.info("testDeleteUser passed.");
    }
   
}