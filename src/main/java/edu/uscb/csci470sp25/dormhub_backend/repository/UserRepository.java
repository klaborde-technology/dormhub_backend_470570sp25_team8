package edu.uscb.csci470sp25.dormhub_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.uscb.csci470sp25.dormhub_backend.model.AppUser;
import edu.uscb.csci470sp25.dormhub_backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	 Optional<User> findByAppUser(AppUser appUser);
}
