package org.apache.dubbo.samples.seata.user.service;

import org.apache.dubbo.samples.seata.api.dto.UserLoginRequest;
import org.apache.dubbo.samples.seata.api.dto.UserRegisterRequest;
import org.apache.dubbo.samples.seata.user.entity.User;
import org.apache.dubbo.samples.seata.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(UserRegisterRequest input) {
        User user = new User(
                input.getName(),
                input.getEmail(),
                passwordEncoder.encode(input.getPassword())
        );
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User authenticate(UserLoginRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow();
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }
}
