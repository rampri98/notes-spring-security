package com.example.notes_spring_security.controller;

import com.example.notes_spring_security.payload.AuthRequest;
import com.example.notes_spring_security.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        return authService.register(authRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {
        return authService.authenticateUser(authRequest);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> reAuthenticateUser(@RequestBody AuthRequest authRequest) {
        return authService.reAuthenticateUser(authRequest.getRefreshToken());
    }
}
