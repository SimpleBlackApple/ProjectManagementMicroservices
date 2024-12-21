package org.apache.dubbo.samples.seata.user.controller;

import org.apache.dubbo.samples.seata.api.dto.UserCreateBody;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.apache.dubbo.samples.seata.user.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.apache.dubbo.samples.seata.api.UserService;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.dto.UserRegisterRequest;
import org.apache.dubbo.samples.seata.api.dto.UserLoginRequest;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(currentUser, userDTO);
        
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Integer userId,
            @RequestBody UserUpdateBody userUpdateBody) {
        try {
            return ResponseEntity.ok(userService.updateUser(userId, userUpdateBody));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            }
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer userId) {
        try {
            userService.deleteUser(userId, false);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Cannot delete user who owns projects")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
            }
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            }
            throw e;
        }
    }

    @DeleteMapping("/{id}/force")
    public ResponseEntity<?> forceDeleteUser(@PathVariable("id") Integer userId) {
        try {
            userService.deleteUser(userId, true);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            }
            throw e;
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }
}