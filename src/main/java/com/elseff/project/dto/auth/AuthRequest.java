package com.elseff.project.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@AllArgsConstructor
public class AuthRequest {

    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    @NotEmpty(message = "email shouldn't be a empty")
    private String email;

    @NotNull(message = "password shouldn't be a null")
    @NotEmpty(message = "password shouldn't be a empty")
    @Size(min = 4, message = "password size should be greater than 4")
    private String password;
}
