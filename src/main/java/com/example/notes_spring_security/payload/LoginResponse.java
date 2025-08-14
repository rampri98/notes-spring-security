package com.example.notes_spring_security.payload;

import com.example.notes_spring_security.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String jwtToken;
    private String refreshToken;
    private String username;
    private List<Role> roles;
}
