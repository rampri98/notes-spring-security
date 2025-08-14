package com.example.notes_spring_security.payload;

import com.example.notes_spring_security.entity.Role;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Data
public class AuthRequest {
    private String username;
    private String password;
    private Set<Role> roles = new HashSet<>();
}
