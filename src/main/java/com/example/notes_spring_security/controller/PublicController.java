package com.example.notes_spring_security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
class PublicController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello, public!";
    }
}