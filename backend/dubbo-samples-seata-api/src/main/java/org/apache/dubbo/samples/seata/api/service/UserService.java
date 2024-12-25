package org.apache.dubbo.samples.seata.api.service;

import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Integer userId, UserUpdateBody userUpdateBody);
    void deleteUser(Integer userId, boolean force);
    void deleteUserRollback(Integer userId);
    UserDTO getUserById(Integer userId);
} 