package com.example.demo.RequestResponseObjects;

import java.time.LocalDateTime;

public class UserPayload {
	private String email;
	private String first_name;
	private String userId;
	private LocalDateTime accountCreated;

	public UserPayload(String email, String first_name,String userId, LocalDateTime accountCreated) {
		this.email = email;
		this.userId = userId;
		this.first_name = first_name;
		this.accountCreated = accountCreated;
	}
	
	
	public UserPayload() {
		super();
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getFirst_name() {
		return first_name;
	}


	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public LocalDateTime getAccountCreated() {
		return accountCreated;
	}


	public void setAccountCreated(LocalDateTime accountCreated) {
		this.accountCreated = accountCreated;
	}


	@Override
	public String toString() {
		return "UserPayload [email=" + email + ", first_name=" + first_name + ", userId=" + userId + ", accountCreated="
				+ accountCreated + "]";
	}


	
	
	
}