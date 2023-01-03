package com.elseff.project.controller.auth;

import com.elseff.project.dto.auth.AuthRequest;
import com.elseff.project.dto.auth.AuthResponse;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication controller", description = "Registering and signing in to accounts")
public class AuthController {

    private final AuthService authService;

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
                                         UserAllFieldsDto userAllFieldsDto) {
        return authService.register(userAllFieldsDto);
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
                                      AuthRequest authRequest) {
        return authService.login(authRequest);
    }
}
