package com.elseff.project.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "User credentials request")
public class AuthRequest {

    @Schema(description = "User email")
    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    private String email;

    @Schema(description = "User password")
    @NotNull(message = "password shouldn't be a null")
    @Size(min = 4, message = "password size should be greater than 4")
    private String password;
}
