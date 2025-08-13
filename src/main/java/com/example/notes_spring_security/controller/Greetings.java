package com.example.notes_spring_security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Greetings {
    @GetMapping("/hello")
    public String getHelloWorld() {
        return "Hello World!";
    }
}
