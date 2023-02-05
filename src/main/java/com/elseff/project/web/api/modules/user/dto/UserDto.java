package com.elseff.project.web.api.modules.user.dto;

import com.elseff.project.persistense.Role;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String country;

    private String password;

    private Timestamp registrationDate;

    private Set<Role> roles;

    private List<ArticleDto> articles;
}
