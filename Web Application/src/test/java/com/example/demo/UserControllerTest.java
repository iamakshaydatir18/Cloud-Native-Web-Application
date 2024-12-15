package com.example.demo;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.RequestResponseObjects.UserRequest;
import com.example.demo.RequestResponseObjects.UserResponse;
import com.example.demo.controller.UserController;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.example.demo.util.PasswordUtil;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.services.sns.SnsClient;

class UserControllerTest {

    @InjectMocks
    UserController userController;

    @Mock
    UserService userService;
    
    @Mock
    NotificationService notificationService;
    
    @Mock
    private SnsClient snsClient;
    
    @Mock
    MeterRegistry meterRegistry;
    
    @Mock
    Counter counter; 
    
    @Mock
    HttpServletRequest request;
    
    @Mock
    BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); 
        userController.checkHeaders(); 
        when(meterRegistry.counter("POST.User.counter")).thenReturn(counter);
        when(meterRegistry.counter("GET.User.counter")).thenReturn(counter);
    }
    

   
    @Test
    void testCreateUser_Success() {
        // Prepare test data
        UserRequest userRequest = new UserRequest();
        userRequest.setFirst_name("John");
        userRequest.setLast_name("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setPassword("password123");

        User user = new User("John", "Doe", "john.doe@example.com", "password123");
        
        // Mocking UserService methods
        when(userService.getUserByEmail(userRequest.getEmail())).thenReturn(null); 
        when(userService.createUser(any(User.class))).thenReturn(user);
        when(userService.getConnection()).thenReturn(true); // Mocking connection
        
        // Mocking HTTP request behavior
        when(request.getQueryString()).thenReturn(null);
        
        // Prepare headers
        Map<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/json");
        
        when(notificationService.PublishMessage(any(User.class))).thenReturn(null);
    
        // Call the createUser method on the controller
        ResponseEntity<UserResponse> response = userController.createUser(userRequest, null, null, request, headers);

        // Assertions
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirst_name());
    }

  
    @Test
    void testCreateUser_UserRequestNull() {
    	
        Map<String, String> headers = new HashMap<>();
        when(request.getQueryString()).thenReturn(null);
        when(userService.getConnection()).thenReturn(true);
       
        
        ResponseEntity<UserResponse> response = userController.createUser(null, null, null, request, headers);
       
        System.out.println(" response is - "+ response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

   
    @Test
    void testCreateUser_EmailExists() {
    	
        UserRequest userRequest = new UserRequest();
        userRequest.setFirst_name("John");
        userRequest.setLast_name("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setPassword("password123");

        when(userService.getConnection()).thenReturn(true);
        when(userService.getUserByEmail(userRequest.getEmail())).thenReturn(new User());

     
        Map<String, String> headers = new HashMap<>();
        when(request.getQueryString()).thenReturn(null);
        when(userService.createUser(null)).thenReturn(null);

        ResponseEntity<UserResponse> response = userController.createUser(userRequest, null, null, request, headers);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

   
//    @Test
//    void testCreateUser_InvalidHeaders() {
//       
//        UserRequest userRequest = new UserRequest();
//        userRequest.setFirst_name("John");
//        userRequest.setLast_name("Doe");
//        userRequest.setEmail("john.doe@example.com");
//        userRequest.setPassword("password123");
//
//       
//        Map<String, String> headers = new HashMap<>();
//        headers.put("invalid-header", "some-value");
//
//        when(userService.getConnection()).thenReturn(true);
//        ResponseEntity<UserResponse> response = userController.createUser(userRequest, null, null, request, headers);
//
//       
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    }
//    
  
    
    
    @Test
    void testGetUser_MissingAuthHeader() {
        Map<String, String> headers = new HashMap<>();
        when(request.getQueryString()).thenReturn(null);
        when(userService.getConnection()).thenReturn(true);
        ResponseEntity<UserResponse> response = userController.getUser(null, null, null, request, headers);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}

