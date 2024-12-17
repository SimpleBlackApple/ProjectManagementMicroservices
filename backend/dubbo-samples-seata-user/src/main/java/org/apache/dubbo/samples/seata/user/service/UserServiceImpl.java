package org.apache.dubbo.samples.seata.user.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.UserCreateBody;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.apache.dubbo.samples.seata.user.entity.User;
import org.apache.dubbo.samples.seata.user.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.apache.dubbo.samples.seata.api.dto.UserRegisterRequest;
import org.apache.dubbo.samples.seata.api.dto.UserLoginRequest;
import org.apache.dubbo.samples.seata.api.dto.UserLoginResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @DubboReference(check = false)
    private ProjectService projectService;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserDTO getUserById(Integer userId) {
        return userRepository.findById(userId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO createUser(UserCreateBody userCreateBodyBody) {
        User user = new User();
        BeanUtils.copyProperties(userCreateBodyBody, user);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Integer userId, UserUpdateBody userUpdateBody) {
        return userRepository.findById(userId)
                .map(user -> {
                    BeanCopyUtils.copyNonNullProperties(userUpdateBody, user);
                    return convertToDTO(userRepository.save(user));
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @GlobalTransactional
    public void deleteUser(Integer userId, boolean force) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 如果不是强制删除，检查用户是否是项目所有者
        if (!force && projectService.isUserProjectOwner(userId)) {
            throw new RuntimeException("Cannot delete user who owns projects. Use force delete if needed.");
        }

        // 处理项目相关的数据
        projectService.handleUserDeletion(userId);

        // 删除用户数据
        userRepository.delete(user);
    }

    @Override
    @GlobalTransactional
    public void deleteUserRollback(Integer userId) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 处理项目相关的数据
        projectService.handleUserDeletion(userId);

        // 删除用户数据
        userRepository.delete(user);
        
        // 抛出异常触发回滚
        throw new RuntimeException("Simulated error for testing rollback");
    }

    @Override
    public UserDTO register(UserRegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // 创建新用户
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .realName(request.getRealName())
                .createdAt(LocalDateTime.now())
                .status("ACTIVE")
                .build();
        
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // 返回登录响应
        UserLoginResponse response = new UserLoginResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
