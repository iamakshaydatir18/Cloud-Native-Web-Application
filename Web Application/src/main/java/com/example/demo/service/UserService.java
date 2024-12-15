package com.example.demo.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.respository.UserRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

@Service
public class UserService {

    private UserRepository userRepository;  
    private final BCryptPasswordEncoder passwordEncoder;
    private final MeterRegistry meterRegistry;
    
    @Autowired
    private DataSource dataSource;
	
    
    public UserService(UserRepository userRepository , MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.meterRegistry = meterRegistry;
    }
    
  
    public List<User> getAllUsers() {
    	
    	 Timer.Sample sample = Timer.start(meterRegistry);
    	 
    	 try {
             return userRepository.findAll();
         } finally {
             sample.stop(meterRegistry.timer("db.query.time", "operation", "getAllUsers"));
         }
    }

   
    public User createUser(User user) {
        
    	Timer.Sample sample = Timer.start(meterRegistry);
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setAccount_created(LocalDateTime.now());
            user.setAccount_updated(LocalDateTime.now());
            return userRepository.save(user);
        } finally {
            sample.stop(meterRegistry.timer("db.query.time", "operation", "createUser"));
        }
        
    }
    
    public String encodePass(String password) {
    	
    	return passwordEncoder.encode(password);
    	
    }
    
    
    public User getUserByEmail(String email) {
    	 Timer.Sample sample = Timer.start(meterRegistry);
         try {
             return userRepository.getUserByEmail(email);
         } finally {
             sample.stop(meterRegistry.timer("db.query.time", "operation", "getUserByEmail"));
         }
    }
    
    public User updateUser(User user) {
    	 Timer.Sample sample = Timer.start(meterRegistry);
         try {
             user.setAccount_updated(LocalDateTime.now());
             return userRepository.save(user);
         } finally {
             sample.stop(meterRegistry.timer("db.query.time", "operation", "updateUser"));
         }
    }
    
    public boolean getConnection() {
    	
    	Timer.Sample sample = Timer.start(meterRegistry);
        boolean success = false;
        try (Connection connection = dataSource.getConnection()) {
            success = true;
        } catch (SQLException e) {
            success = false;
        } finally {
            sample.stop(meterRegistry.timer("db.query.time", "operation", "getConnection", "status", success ? "success" : "failure"));
        }
        return success;
    }
    
    public boolean verifyUserEmail(String token) {
        Optional<User> userOptional = userRepository.findByEmailVerificationToken(token);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Get the current time as LocalDateTime
            LocalDateTime currentDateTime = LocalDateTime.now();
            
            // Check if the expiry time is after the current time
            if (user.getEmailVerificationTokenExpiry().isAfter(currentDateTime)) {
                user.setEmailVerified(true);
                user.setEmailVerificationToken(null);
                user.setEmailVerificationTokenExpiry(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
