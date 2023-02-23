package com.elseff.project.web.api.modules.auth.controller;

import com.elseff.project.web.api.modules.auth.dto.AuthLoginRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthRegisterRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthResponse;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CrossOrigin(origins = {"http://192.168.100.3:4200", "http://localhost:4200"})
@Tag(name = "Authentication controller", description = "Registering and signing in to accounts")
public class AuthController {

    AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register",
            description = "Sign up new account",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "User has been successfully registered",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "User not valid", content = @Content),
            }
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Parameter(description = "User for registration")
                                 @RequestBody
                                 @Valid
                                         AuthRegisterRequest authRegisterRequest) {
        return authService.register(authRegisterRequest);
    }

    @Operation(summary = "Log in",
            description = "Log in account",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "User credentials not valid", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            }
    )
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse login(@Parameter(description = "User credentials")
                              @RequestBody
                              @Valid
                                      AuthLoginRequest authLoginRequest) {
        return authService.login(authLoginRequest);
    }
}
