package com.example.demo.respository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	public User getUserByEmail(String email);
	
	Optional<User> findByEmailVerificationToken(String token);
}
