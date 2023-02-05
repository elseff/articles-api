package com.elseff.project.web.api.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateRequest {

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    private String firstName;

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @Size(min = 2, max = 40, message = "lastname size should be between 2 and 40")
    private String lastName;

    @Email(message = "email should be valid")
    private String email;

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "country should be valid")
    @Size(min = 3, max = 74, message = "country size should be between 3 and 73")
    private String country;
}
