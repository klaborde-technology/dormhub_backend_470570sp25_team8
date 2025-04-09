package edu.uscb.csci470sp25.dormhub_backend.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.uscb.csci470sp25.dormhub_backend.model.UserTask;

import java.util.List;

public interface UserTaskRepository extends JpaRepository<UserTask, Long> {
	List<UserTask> findByStatus(Boolean status, Sort sort);
    List<UserTask> findByUserId(Long userId, Sort sort);
    List<UserTask> findByUserIdAndStatus(Long userId, Boolean status, Sort sort);
}