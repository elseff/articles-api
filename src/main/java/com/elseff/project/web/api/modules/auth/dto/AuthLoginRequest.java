package com.elseff.project.web.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User credentials request")
public class AuthLoginRequest {

    @Schema(description = "User email")
    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    private String email;

    @Schema(description = "User password")
    @NotNull(message = "password shouldn't be a null")
    @Size(min = 4, message = "password size should be greater than 4")
    private String password;
}
