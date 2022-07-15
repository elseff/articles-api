package com.elseff.project.dto.user;

import lombok.*;

import javax.validation.constraints.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Positive(message = "id should be a greater than 0")
    private Long id;

    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @NotNull(message = "firstname shouldn't be a null")
    @NotEmpty(message = "firstname shouldn't be a empty")
    private String firstName;

    @Size(min = 2, max = 40,message = "firstname size should be between 2 and 40")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @NotNull(message = "lastname shouldn't be a null")
    @NotEmpty(message = "lastname shouldn't be a empty")
    private String lastName;

    @NotNull(message = "country shouldn't be a null")
    @NotEmpty(message = "country shouldn't be a empty")
    private String country;
}
