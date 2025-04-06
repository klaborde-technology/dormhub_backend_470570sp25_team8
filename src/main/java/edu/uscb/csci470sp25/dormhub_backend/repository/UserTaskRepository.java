package edu.uscb.csci470sp25.dormhub_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {
	
}