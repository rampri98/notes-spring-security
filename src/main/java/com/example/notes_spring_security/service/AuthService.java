package com.example.notes_spring_security.service;

import com.example.notes_spring_security.config.jwt.JwtUtils;
import com.example.notes_spring_security.entity.Role;
import com.example.notes_spring_security.payload.AuthRequest;
import com.example.notes_spring_security.payload.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private UserDetailsManager userDetailsManager;
    private PasswordEncoder passwordEncoder;
    private JwtUtils jwtUtils;
    private AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
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

    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateJwtTokenFromUsername(userDetails);
        String refreshToken = jwtUtils.generateJwtTokenFromUsername(userDetails);

        List<Role> roles = userDetails.getAuthorities().stream()
                .map(item -> Role.valueOf(item.getAuthority()))
                .toList();

        LoginResponse response = LoginResponse.builder()
                                    .username(userDetails.getUsername())
                                    .roles(roles)
                                    .jwtToken(jwtToken)
                                    .refreshToken(refreshToken)
                                    .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> reAuthenticateUser(@RequestBody String refreshToken) {
        Map<String, String> tokens = jwtUtils.generateJwtTokenFromRefreshToken(refreshToken);
        if(tokens == null) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(tokens);
    }
}
