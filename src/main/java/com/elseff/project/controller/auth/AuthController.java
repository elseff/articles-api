package com.elseff.project.controller.auth;

import com.elseff.project.dto.auth.AuthRequest;
import com.elseff.project.dto.auth.AuthResponse;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.service.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {"http://192.168.100.4:4200", "http://localhost:4200"})
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@RequestBody @Valid UserAllFieldsDto userAllFieldsDto) {
        return authService.register(userAllFieldsDto);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@RequestBody @Valid AuthRequest authRequest) {
        return authService.login(authRequest);
    }
}
