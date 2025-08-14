package com.example.notes_spring_security.config.security_jpa;

import com.example.notes_spring_security.entity.Role;
import com.example.notes_spring_security.entity.User;
import com.example.notes_spring_security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import java.util.Set;
import java.util.stream.Collectors;

public class JpaUserDetailsManager implements UserDetailsManager {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public JpaUserDetailsManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }

    @Override
    public void createUser(UserDetails userDetails) {
        if (userRepository.findByUsername(userDetails.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists: " + userDetails.getUsername());
        }

        User user = new User();
        user.setUsername(userDetails.getUsername());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        user.setEnabled(true);

        // Convert authorities to role names
        Set<Role> roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> Role.valueOf(grantedAuthority.getAuthority()))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public void updateUser(UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));

        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));

        Set<Role> roles = userDetails.getAuthorities().stream()
                .map(grantedAuthority -> Role.valueOf(grantedAuthority.getAuthority()))
                .collect(Collectors.toSet());
        user.setRoles(roles);

        userRepository.save(user);
    }

    @Override
    public void deleteUser(String username) {
        userRepository.findByUsername(username)
                .ifPresent(userRepository::delete);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Change password not implemented here");
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
}