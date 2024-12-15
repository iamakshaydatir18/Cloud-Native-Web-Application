package com.example.demo;


import com.example.demo.RequestResponseObjects.UserRequest;
import com.example.demo.controller.UserController;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.example.demo.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class) 
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @InjectMocks
    private UserController userController;
    
    @MockBean
    NotificationService notificationService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
    	System.out.println("*************Running Integration Test******************");
        objectMapper = new ObjectMapper();
    }

    @Test
    @Order(1)
    public void testCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest("John", "Doe", "password123", "john@example.com");
        User user = new User("1L", "John", "Doe", "john@example.com", "hashedPassword", null, null);
        
        when(userService.getUserByEmail(userRequest.getEmail())).thenReturn(null); 
        when(userService.getConnection()).thenReturn(true);
        when(userService.createUser(any(User.class))).thenReturn(user); 
        when(notificationService.PublishMessage(any(User.class))).thenReturn(null);

        mockMvc.perform(post("/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.first_name").value("John"))
                .andExpect(jsonPath("$.last_name").value("Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @Order(2)
    public void testGetUser() throws Exception {
        String email = "john@example.com";
        String password = "password123";
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        User existingUser = new User("1L", "John", "Doe", email, PasswordUtil.hashPassword("password123"), null, null); 
        existingUser.setEmailVerified(true);

        when(userService.getUserByEmail(email)).thenReturn(existingUser); 
        when(userService.getConnection()).thenReturn(true);
        
      

        mockMvc.perform(get("/v1/user/self")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("John"))
                .andExpect(jsonPath("$.last_name").value("Doe"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    @Order(3)
    public void testUpdateUser() throws Exception {
        String email = "john@example.com";
        String password = "password123";
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((email + ":" + password).getBytes());
        UserRequest userRequest = new UserRequest("John", "Smith", "newPassword123", null); 
        User updatedUser = new User("1L", "John", "Smith", email,PasswordUtil.hashPassword("password123"), null, null);
        updatedUser.setEmailVerified(true);

        when(userService.getUserByEmail(email)).thenReturn(updatedUser); 
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser); 
        when(userService.getConnection()).thenReturn(true);

        mockMvc.perform(put("/v1/user/self")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNoContent());
    }
}
