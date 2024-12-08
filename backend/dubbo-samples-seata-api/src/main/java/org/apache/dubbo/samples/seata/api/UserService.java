package org.apache.dubbo.samples.seata.api;

import org.apache.dubbo.samples.seata.api.dto.UserCreateBody;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;

import java.util.List;

public interface UserService {
    UserDTO getUserById(Integer userId);
    List<UserDTO> getAllUsers();
    UserDTO createUser(UserCreateBody userCreateBody);
    UserDTO updateUser(Integer userId, UserUpdateBody userUpdateBody);
    void deleteUser(Integer userId, boolean force);
    void deleteUserRollback(Integer userId);
} 