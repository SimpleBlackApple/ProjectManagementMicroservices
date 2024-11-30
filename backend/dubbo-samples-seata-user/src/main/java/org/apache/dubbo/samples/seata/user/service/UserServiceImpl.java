package org.apache.dubbo.samples.seata.user.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.samples.seata.api.dto.UserCreateBody;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.apache.dubbo.samples.seata.user.entity.User;
import org.apache.dubbo.samples.seata.user.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                    BeanUtils.copyProperties(userUpdateBody, user, "userId");
                    return convertToDTO(userRepository.save(user));
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}
