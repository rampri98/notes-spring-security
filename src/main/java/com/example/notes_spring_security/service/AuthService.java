package com.example.notes_spring_security.service;

import com.example.notes_spring_security.entity.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserDetailsManager userDetailsManager;

    public ResponseEntity<?> register(AuthRequest authRequest) {
        if (userDetailsManager.userExists(authRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        UserDetails user = User.withUsername(authRequest.getUsername())
                .password(authRequest.getPassword())
                .roles("USER")
                .build();

        userDetailsManager.createUser(user);

        return ResponseEntity
                .ok()
                .body("Created successfully!!");
    }
}
