package com.elseff.project.controller.auth;

import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.service.uth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://192.168.100.4:4200", "http://localhost:4200"})
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserAllFieldsDto register(@RequestBody @Valid UserAllFieldsDto userAllFieldsDto) {
        return authService.register(userAllFieldsDto);
    }
}
