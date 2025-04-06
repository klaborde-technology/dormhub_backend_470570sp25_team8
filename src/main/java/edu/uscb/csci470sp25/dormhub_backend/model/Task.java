package edu.uscb.csci470sp25.dormhub_backend.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @OneToMany(mappedBy = "task")
    @JsonIgnore
    private List<UserTask> userTasks = new ArrayList<>();

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    // Methods to manage the list of UserTask assignments
    public List<UserTask> getUserTasks() {
        return userTasks;
    }
    
    public void addUserTask(UserTask userTask) {
        userTasks.add(userTask);
    }
}