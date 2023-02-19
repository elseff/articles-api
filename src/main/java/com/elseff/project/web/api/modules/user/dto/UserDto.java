package com.elseff.project.web.api.modules.user.dto;

import com.elseff.project.persistense.Role;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;

    String firstName;

    String lastName;

    String email;

    String country;

    String password;

    Timestamp registrationDate;

    Set<Role> roles;

    List<ArticleDto> articles;
}
