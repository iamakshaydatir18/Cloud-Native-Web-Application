package com.example.demo.RequestResponseObjects;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {

    @NotBlank(message = "First name is required")
    private String first_name;

    @NotBlank(message = "Last name is required")
    private String last_name;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    

	public UserRequest() {
		super();
	}

	public UserRequest(@NotBlank(message = "First name is required") String first_name,
			@NotBlank(message = "Last name is required") String last_name,
			@NotBlank(message = "Password is required") String password,
			@NotBlank(message = "Email is required") @Email(message = "Email should be valid") String email) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.password = password;
		this.email = email;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserRequest [first_name=" + first_name + ", last_name=" + last_name + ", password=" + password
				+ ", email=" + email + "]";
	}

	

   
    
}
