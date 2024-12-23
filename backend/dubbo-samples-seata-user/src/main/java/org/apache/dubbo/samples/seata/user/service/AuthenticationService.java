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
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.TaskService;
import org.apache.dubbo.samples.seata.user.exception.AuthenticationException;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @DubboReference(check = false)
    private ProjectService projectService;

    @DubboReference(check = false)
    private TaskService taskService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GlobalTransactional
    public User signup(UserRegisterRequest input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new AuthenticationException("邮箱已被注册", "EMAIL_EXISTS");
        }

        User user = new User(
                input.getName(),
                input.getEmail(),
                passwordEncoder.encode(input.getPassword())
        );
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        
        try {
            projectService.syncNewUser(savedUser.getId(), savedUser.getEmail());
            taskService.syncNewUser(savedUser.getId(), savedUser.getEmail());
        } catch (Exception e) {
            throw new AuthenticationException("同步用户数据失败: " + e.getMessage(), "SYNC_FAILED");
        }

        return savedUser;
    }

    public User authenticate(UserLoginRequest input) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getEmail(),
                            input.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new AuthenticationException("邮箱或密码错误", "INVALID_CREDENTIALS");
        }

        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new AuthenticationException("用户不存在", "USER_NOT_FOUND"));
        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }
}
