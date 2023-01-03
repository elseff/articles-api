package com.elseff.project.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "firstname shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    private String firstName;

    @NotNull(message = "lastname shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @Size(min = 2, max = 40, message = "lastname size should be between 2 and 40")
    private String lastName;

    @Size(min = 3, max = 74)
    @NotNull(message = "country shouldn't be a null")
    private String country;
}
