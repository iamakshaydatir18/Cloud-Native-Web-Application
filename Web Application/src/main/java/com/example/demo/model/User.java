package com.example.demo.model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.UUID;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;

@Entity
public class User {

	@Id
	private String id;
	@Column(name = "first_name", nullable = false)
	private String first_name;
	@Column(name = "last_name", nullable = false)
	private String last_name;
	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false)
	private String password;

	@Column(name = "account_created", nullable = false)
	private LocalDateTime account_created;
	@Column(name = "account_updated", nullable = false)
	private LocalDateTime account_updated;

	// Email varification
	@Column(name = "email_verification_token")
	private String emailVerificationToken;

	@Column(name = "email_verified")
	private Boolean emailVerified = false;

	@Column(name = "email_verification_token_expiry")
	private LocalDateTime emailVerificationTokenExpiry;

	public User() {
		super();
	}

	public User(String id, String first_name, String last_name, String email, String password,
			LocalDateTime account_created, LocalDateTime account_updated) {
		super();
		this.id = id;
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.password = password;
		this.account_created = account_created;
		this.account_updated = account_updated;
	}

	public User(String first_name, String last_name, String email, String password) {
		super();
		this.id = UUID.randomUUID().toString();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.password = password;
		this.account_created = LocalDateTime.now();
		this.account_updated = LocalDateTime.now();
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	@PreUpdate
	public void setLastUpdated() {
		this.account_updated = LocalDateTime.now();
	}

	public String getEmailVerificationToken() {
		return emailVerificationToken;
	}

	public void setEmailVerificationToken(String emailVerificationToken) {
		this.emailVerificationToken = emailVerificationToken;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public LocalDateTime getEmailVerificationTokenExpiry() {
		return emailVerificationTokenExpiry;
	}

	public void setEmailVerificationTokenExpiry(LocalDateTime emailVerificationTokenExpiry) {
		this.emailVerificationTokenExpiry = emailVerificationTokenExpiry;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", first_name=" + first_name + ", last_name=" + last_name + ", email=" + email
				+ ", password=" + password + ", account_created=" + account_created + ", account_updated="
				+ account_updated + "]";
	}

}
