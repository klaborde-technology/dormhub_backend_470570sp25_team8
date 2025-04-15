package edu.uscb.csci470sp25.dormhub_backend.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_tasks")
public class UserTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")  // Add this annotation
    private LocalDate deadline;
    
    private Boolean status;

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public LocalDate getDeadline() {
        return deadline;
    }
    
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    
    public Boolean isStatus() {
        return status;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
}