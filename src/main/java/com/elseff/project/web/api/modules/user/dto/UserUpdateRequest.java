package com.elseff.project.web.api.modules.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    String firstName;

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @Size(min = 2, max = 40, message = "lastname size should be between 2 and 40")
    String lastName;

    @Email(message = "email should be valid")
    String email;

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "country should be valid")
    @Size(min = 3, max = 74, message = "country size should be between 3 and 73")
    String country;
}
