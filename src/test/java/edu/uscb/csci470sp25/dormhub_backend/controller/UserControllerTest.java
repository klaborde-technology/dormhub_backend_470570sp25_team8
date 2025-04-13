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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

import edu.uscb.csci470sp25.dormhub_backend.model.AppUser;
import edu.uscb.csci470sp25.dormhub_backend.repository.AppUserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "admin", authorities = {"ADMIN"})
public class UserControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private AppUserRepository appUserRepository;

    private Long testUserId;
    private Long testAppUserId;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        
        // Create the test AppUser
        AppUser testAppUser = new AppUser();
        testAppUser.setEmail("privilegeduser@example.com");
        testAppUser.setPassword("password");
        testAppUser.setRole("PRIVILEGED_USER");
        testAppUser = appUserRepository.save(testAppUser);
        testAppUserId = testAppUser.getId();
        
        // Insert a test user into the database and retrieve its ID
        String newUserJson = String.format(
        		"{\"username\":\"johndoe\",\"name\":\"John Doe\",\"email\":\"johndoe@example.com\",\"appUser\":{\"id\":%d}}",
        		testAppUserId
        );
       
        String response = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the ID from the response (assuming the response contains the user ID as "id")
        Integer id = JsonPath.read(response, "$.id");
        testUserId = id.longValue();

        logger.info("Setup complete. Test user ID: {}", testUserId);
    }

    @Test
    public void testGetUserById() throws Exception {
        logger.info("Testing getUserById with ID: {}", testUserId);

        mockMvc.perform(get("/user/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserId));

        logger.info("testGetUserById passed.");
    }

    @Test
    public void testCreateUser() throws Exception {
        String newUserJson = String.format(
        		"{\"username\":\"janedoe\",\"name\":\"Jane Doe\",\"appUser\":{\"id\":%d}}",
        		testAppUserId
        );
        
        logger.info("Testing createUser with payload: {}", newUserJson);
        
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("privilegeduser@example.com"));

        logger.info("testCreateUser passed.");
    }

    @Test
    public void testGetAllUsers() throws Exception {
        logger.info("Testing getAllUsers");

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        logger.info("testGetAllUsers passed.");
    }

    @Test
    public void testUpdateUser() throws Exception {
        String updatedUserJson = String.format(
        		"{\"username\":\"johndoe_updated\",\"name\":\"John Doe Updated\",\"appUser\":{\"id\":%d}}",
        		testAppUserId
        
        );
        
        logger.info("Testing updateUser with ID: {} and payload: {}", testUserId, updatedUserJson);

        mockMvc.perform(put("/user/{id}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe_updated"))
                .andExpect(jsonPath("$.name").value("John Doe Updated"))
                .andExpect(jsonPath("$.email").value("privilegeduser@example.com"));

        logger.info("testUpdateUser passed.");
    }

    @Test
    public void testDeleteUser() throws Exception {
        logger.info("Testing deleteUser with ID: {}", testUserId);

        mockMvc.perform(delete("/user/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(content().string("User with id " + testUserId + " has been deleted successfully."));

        logger.info("testDeleteUser passed.");
    }
}