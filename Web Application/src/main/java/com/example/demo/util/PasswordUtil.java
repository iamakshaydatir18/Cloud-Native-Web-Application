package com.example.demo.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    
    public static String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }


}