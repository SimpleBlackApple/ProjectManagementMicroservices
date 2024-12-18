package org.apache.dubbo.samples.seata.user.controller;

import org.apache.dubbo.samples.seata.api.dto.UserCreateBody;
import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable("id") Integer userId) {
        try {
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
            }
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserCreateBody userCreateBody) {
        return ResponseEntity.ok(userService.createUser(userCreateBody));
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            return ResponseEntity.ok(userService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            return ResponseEntity.ok(userService.login(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getMessage());
    }
}