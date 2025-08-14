package com.example.notes_spring_security.controller;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
class UserController {
    // URL-based role restriction is already in SecurityConfig
    @GetMapping("/hello")
    public String dashboard() {
        return "Hello, USER or ADMIN!";
    }

    // Method-level role restriction
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/adminOnly")
    public String adminOnlyMethod() {
        return "Only ADMIN can see this (method-level)";
    }

    // Post-authorization example: check return data before sending
    @PostAuthorize("returnObject.contains(authentication.name)")
    @GetMapping("/myData")
    public String getMyData() {
        // Suppose this comes from DB
        return "Sensitive data for user: user";
    }
}