package com.example.demo.RequestResponseObjects;

import java.time.LocalDateTime;

public class UserResponse {
    private String id;
    private String first_name;
    private String last_name;
    private String email;
    private LocalDateTime account_created;
    private LocalDateTime account_updated;

    // Constructor
    public UserResponse(String id, String firstName, String lastName, String email, LocalDateTime accountCreated, LocalDateTime accountUpdated) {
        this.id = id;
        this.first_name = firstName;
        this.last_name = lastName;
        this.email = email;
        this.account_created = accountCreated;
        this.account_updated = accountUpdated;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDateTime getAccount_created() {
		return account_created;
	}

	public void setAccount_created(LocalDateTime account_created) {
		this.account_created = account_created;
	}

	public LocalDateTime getAccount_updated() {
		return account_updated;
	}

	public void setAccount_updated(LocalDateTime account_updated) {
		this.account_updated = account_updated;
	}

   
}
