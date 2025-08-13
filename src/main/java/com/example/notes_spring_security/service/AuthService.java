package com.example.notes_spring_security.service;

import com.example.notes_spring_security.entity.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.beans.Encoder;

@Service
public class AuthService {
    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(AuthRequest authRequest) {
        if (userDetailsManager.userExists(authRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        UserDetails user = User.withUsername(authRequest.getUsername())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .roles("USER")
                .build();

        userDetailsManager.createUser(user);

        return ResponseEntity
                .ok()
                .body("Created successfully!!");
    }
}
