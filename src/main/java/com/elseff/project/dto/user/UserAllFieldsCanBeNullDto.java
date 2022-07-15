package com.elseff.project.dto.user;

import lombok.*;

import javax.validation.constraints.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAllFieldsCanBeNullDto {
    @Positive(message = "id should be a greater than 0")
    private Long id;

    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    private String firstName;

    @Size(min = 2, max = 40,message = "firstname size should be between 2 and 40")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    private String lastName;

    @Email(message = "email should be valid")
    private String email;

    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "country should be valid")
    private String country;
}
