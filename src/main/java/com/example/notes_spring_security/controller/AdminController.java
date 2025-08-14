package com.example.notes_spring_security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
class AdminController {
    @GetMapping("/hello")
    public String settings() {
        return "Hello, ADMIN!";
    }
}