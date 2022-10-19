package com.elseff.project.dto.user;

import lombok.*;

import javax.validation.constraints.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAllFieldsDto {
    @Positive(message = "id should be a greater than 0")
    private Long id;

    @NotNull(message = "firstname shouldn't be a null")
    @NotEmpty(message = "firstname shouldn't be a empty")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    private String firstName;

    @NotNull(message = "lastname shouldn't be a null")
    @NotEmpty(message = "lastname shouldn't be a empty")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    private String lastName;

    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    @NotEmpty(message = "email shouldn't be a empty")
    private String email;

    @NotNull(message = "country shouldn't be a null")
    @NotEmpty(message = "country shouldn't be a empty")
    private String country;

    @NotNull(message = "password shouldn't be a null")
    @NotEmpty(message = "password shouldn't be a empty")
    @Size(min = 4, message = "password size should be greater than 4")
    private String password;
}
