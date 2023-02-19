package com.elseff.project.web.api.modules.article.dto;

import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleDto {

    Long id;

    String title;

    String description;

    Timestamp createdAt;

    UserDto author;
}
