package org.apache.dubbo.samples.seata.user.controller;

import org.apache.dubbo.samples.seata.api.dto.UserDTO;
import org.apache.dubbo.samples.seata.api.dto.UserLoginRequest;
import org.apache.dubbo.samples.seata.api.dto.UserLoginResponse;
import org.apache.dubbo.samples.seata.api.dto.UserRegisterRequest;
import org.apache.dubbo.samples.seata.user.entity.User;
import org.apache.dubbo.samples.seata.user.service.AuthenticationService;
import org.apache.dubbo.samples.seata.user.service.JwtService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.desktop.UserSessionListener;

@RequestMapping("api/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> register(@RequestBody UserRegisterRequest registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(registeredUser, userDTO);

        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> authenticate(@RequestBody UserLoginRequest loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        UserLoginResponse loginResponse = new UserLoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }
}
