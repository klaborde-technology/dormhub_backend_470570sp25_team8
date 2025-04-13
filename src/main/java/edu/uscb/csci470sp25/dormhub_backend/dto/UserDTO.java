package edu.uscb.csci470sp25.dormhub_backend.dto;

import edu.uscb.csci470sp25.dormhub_backend.model.User;

public class UserDTO {
    private Long id;
	private String username;
    private String name;
    private String email;
    private String role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getAppUser().getEmail();
        this.role = user.getAppUser().getRole();
    }

    // Getters & Setters
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
