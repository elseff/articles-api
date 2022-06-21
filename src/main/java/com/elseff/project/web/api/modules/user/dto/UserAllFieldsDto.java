package com.elseff.project.web.api.modules.user.dto;

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

    @Size(min = 2, max = 40)
    @Pattern(regexp = "([A-Z][a-zA-Z]*)")
    @NotNull(message = "firstname shouldn't be a null")
    @NotEmpty(message = "firstname shouldn't be a empty")
    private String firstName;

    @Size(min = 2, max = 40)
    @Pattern(regexp = "([A-Z][a-zA-Z]*)")
    @NotNull(message = "lastname shouldn't be a null")
    @NotEmpty(message = "lastname shouldn't be a empty")
    private String lastName;

    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    @NotEmpty(message = "email shouldn't be a empty")
    private String email;

    @NotNull(message = "country shouldn't be a null")
    @NotEmpty(message = "country shouldn't be a empty")
    private String country;
}
