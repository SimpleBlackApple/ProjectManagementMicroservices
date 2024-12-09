package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.UserCreateBody;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.apache.dubbo.samples.seata.api.dto.UserRegisterRequest;
import org.apache.dubbo.samples.seata.api.dto.UserLoginRequest;
import org.apache.dubbo.samples.seata.api.dto.UserLoginResponse;

import java.util.List;

public interface UserService {
    UserDTO getUserById(Integer userId);
    List<UserDTO> getAllUsers();
    UserDTO createUser(UserCreateBody userCreateBody);
    UserDTO updateUser(Integer userId, UserUpdateBody userUpdateBody);
    void deleteUser(Integer userId, boolean force);
    void deleteUserRollback(Integer userId);
    UserDTO register(UserRegisterRequest request);
    UserLoginResponse login(UserLoginRequest request);
} 