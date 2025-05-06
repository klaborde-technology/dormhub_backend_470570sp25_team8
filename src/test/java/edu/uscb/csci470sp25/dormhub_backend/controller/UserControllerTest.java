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
import edu.uscb.csci470sp25.dormhub_backend.repository.UserRepository;
 
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {
 
    private static final Logger logger = LoggerFactory.getLogger(UserControllerTest.class);
 
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MockMvc mockMvc;
 
    @Autowired
    private WebApplicationContext webApplicationContext;
 
    private Long testUserId;
 
    @BeforeEach
    public void setup() throws Exception {
    	this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

        String newUserJson = """
        		{
        			"username": "janedoe",
        			"name": "Jane Doe",
        			"email": "janedoe@example.com",
        			"password": "password1234",
        			"role": "PRIVILEGED_USER"
        		}
        	""";
        String userResponse = mockMvc.perform(post("/user")
        		.contentType(MediaType.APPLICATION_JSON)
        		.content(newUserJson))
        		.andExpect(status().isOk())
        		.andReturn()
        		.getResponse()
        		.getContentAsString();

        Integer id = JsonPath.read(userResponse, "$.id");
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
        String uniqueUsername = "janedoe_" + System.currentTimeMillis();  // Make it unique
        String uniqueEmail = "janedoe" + System.currentTimeMillis() + "@example.com";  // Make email unique

        String newUserJson = """
            {
                "username": "%s",
                "name": "Bill Bob",
                "email": "%s",
                "password": "password1234",
                "role": "PRIVILEGED_USER"
            }
        """.formatted(uniqueUsername, uniqueEmail);

        logger.info("Testing createUser with payload: {}", newUserJson);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(uniqueUsername))
                .andExpect(jsonPath("$.name").value("Bill Bob"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("PRIVILEGED_USER"));

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
    	 String updatedUserJson = """
    	        	{
    	        		"username": "johndoe_updated",
    	        		"name": "John Doe Updated",
    	        		"email": "johndoe_updated@example.com",
    	        		"password": "newpassword1234",
    	        		"role": "ADMIN"
    	        	}
    	        """;

    	        logger.info("Testing updateUser with ID: {} and payload: {}", testUserId, updatedUserJson);

    	        mockMvc.perform(put("/user/{id}", testUserId)
    	                .contentType(MediaType.APPLICATION_JSON)
    	                .content(updatedUserJson))
    	                .andExpect(status().isOk())
    	                .andExpect(jsonPath("$.username").value("johndoe_updated"))
    	                .andExpect(jsonPath("$.name").value("John Doe Updated"));

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