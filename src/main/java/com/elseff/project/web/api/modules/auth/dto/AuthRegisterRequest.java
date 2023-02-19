package com.elseff.project.web.api.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "User for register request")
public class AuthRegisterRequest {

    @NotNull(message = "firstname shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    String firstName;

    @NotNull(message = "lastname shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @Size(min = 2, max = 40, message = "lastname size should be between 2 and 40")
    String lastName;

    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    String email;

    @Size(min = 3, max = 74)
    @NotNull(message = "country shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "country should be valid")
    String country;

    @NotNull(message = "password shouldn't be a null")
    @Size(min = 4, message = "password size should be greater than 4")
    String password;
}
