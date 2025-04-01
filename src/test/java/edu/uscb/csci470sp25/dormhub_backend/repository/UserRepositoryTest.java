package edu.uscb.csci470sp25.dormhub_backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional; // Optional is used to avoid NullPointerExceptions

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import edu.uscb.csci470sp25.dormhub_backend.model.User;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindById() {
        // Arrange
        User user = new User();
        user.setName("John Doe");
        user = userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findById(user.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getName());
    }

    @Test
    public void testSave() {
        // Arrange
        User user = new User();
        user.setName("Jane Doe");

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertEquals("Jane Doe", savedUser.getName());
        assertTrue(userRepository.findById(savedUser.getId()).isPresent());
    }

    @Test
    public void testDeleteById() {
        // Arrange
        User user = new User();
        user.setName("John Smith");
        user = userRepository.save(user);
        Long userId = user.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        assertFalse(userRepository.findById(userId).isPresent());
    }
}