package org.apache.dubbo.samples.seata.user.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.service.ProjectService;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.apache.dubbo.samples.seata.api.entity.User;
import org.apache.dubbo.samples.seata.user.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.samples.seata.api.service.UserService;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.util.BeanCopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.apache.dubbo.samples.seata.user.exception.UserOperationException;
import org.apache.dubbo.samples.seata.api.service.TaskService;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @DubboReference(check = false)
    private ProjectService projectService;

    @DubboReference(check = false)
    private TaskService taskService;

    private final BCryptPasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Integer userId, UserUpdateBody userUpdateBody) {
        return userRepository.findById(userId)
                .map(user -> {
                    boolean needNewToken = false;
                    // 检查是否修改了密码
                    if (userUpdateBody.getPassword() != null && !userUpdateBody.getPassword().isEmpty()) {
                        userUpdateBody.setPassword(passwordEncoder.encode(userUpdateBody.getPassword()));
                        needNewToken = true;
                    }

                    // 更新用户信息
                    BeanCopyUtils.copyNonNullProperties(userUpdateBody, user);
                    User savedUser = userRepository.save(user);
                    UserDTO userDTO = convertToDTO(savedUser);

                    // 如果需要，生成新的token
                    if (needNewToken) {
                        String newToken = jwtService.generateToken(savedUser);
                        userDTO.setNewToken(newToken);
                        userDTO.setExpiresIn(jwtService.getExpirationTime());
                    }

                    return userDTO;
                })
                .orElseThrow(() -> new UserOperationException("User not found", "USER_NOT_FOUND"));
    }

    @Override
    @GlobalTransactional
    public void deleteUser(Integer userId, boolean force) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserOperationException("User not found", "USER_NOT_FOUND"));

        // 如果不是强制删除，检查用户是否是项目所有者
        if (!force && projectService.isUserProjectOwner(user.getEmail())) {
            throw new UserOperationException(
                "Cannot delete user who owns projects. Use force delete if needed.",
                "USER_OWNS_PROJECTS"
            );
        }

        try {
            // 先处理项目相关的数据
            projectService.handleUserDeletion(user.getEmail());
            // 删除项目服务中的用户数据
            projectService.removeUserData(user.getEmail());
            // 删除任务服务中的用户数据
            taskService.removeUserData(user.getEmail());
            // 最后删除用户数据
            userRepository.delete(user);
        } catch (Exception e) {
            throw new UserOperationException(
                "Failed to delete user: " + e.getMessage(),
                "DELETE_USER_FAILED"
            );
        }
    }

    @Override
    @GlobalTransactional
    public void deleteUserRollback(Integer userId) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 处理项目相关的数据
        projectService.handleUserDeletion(user.getEmail());

        // 删除用户数据
        userRepository.delete(user);
        
        // 出异常触发回滚
        throw new RuntimeException("Simulated error for testing rollback");
    }

    @Override
    public UserDTO getUserById(Integer userId) {
        return userRepository.findById(userId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new UserOperationException("User not found", "USER_NOT_FOUND"));
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
