package com.example.notes_spring_security.service;

import com.example.notes_spring_security.entity.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.beans.Encoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private UserDetailsManager userDetailsManager;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> register(AuthRequest authRequest) {
        if (userDetailsManager.userExists(authRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        UserDetails user = User.withUsername(authRequest.getUsername())
                .password(authRequest.getPassword())
                .authorities(authRequest.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
                )
                .build();

        userDetailsManager.createUser(user);

        return ResponseEntity
                .ok()
                .body("Created successfully!!");
    }
}
