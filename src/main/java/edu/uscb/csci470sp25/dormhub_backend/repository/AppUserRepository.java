package edu.uscb.csci470sp25.dormhub_backend.repository;

import edu.uscb.csci470sp25.dormhub_backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}