package org.apache.dubbo.samples.seata.user.controller;

import org.apache.dubbo.samples.seata.api.dto.UserUpdateBody;
import org.apache.dubbo.samples.seata.api.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.apache.dubbo.samples.seata.api.service.UserService;
import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.user.exception.UserOperationException;
import org.apache.dubbo.samples.seata.user.dto.ErrorResponse;

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

    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateBody userUpdateBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        try {
            UserDTO updatedUser = userService.updateUser(currentUser.getId(), userUpdateBody);
            return ResponseEntity.ok(updatedUser);
        } catch (UserOperationException e) {
            HttpStatus status = switch (e.getErrorCode()) {
                case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
                default -> HttpStatus.BAD_REQUEST;
            };
            return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        try {
            userService.deleteUser(currentUser.getId(), false);
            return ResponseEntity.ok().build();
        } catch (UserOperationException e) {
            HttpStatus status = switch (e.getErrorCode()) {
                case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
                case "USER_OWNS_PROJECTS" -> HttpStatus.FORBIDDEN;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
            return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }

    @DeleteMapping("/force")
    public ResponseEntity<?> forceDeleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        try {
            userService.deleteUser(currentUser.getId(), true);
            return ResponseEntity.ok().build();
        } catch (UserOperationException e) {
            HttpStatus status = switch (e.getErrorCode()) {
                case "USER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
                default -> HttpStatus.INTERNAL_SERVER_ERROR;
            };
            return ResponseEntity
                .status(status)
                .body(new ErrorResponse(e.getErrorCode(), e.getMessage()));
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "Internal server error"));
    }
}