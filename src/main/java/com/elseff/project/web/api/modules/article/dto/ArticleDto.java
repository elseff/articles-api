package com.elseff.project.web.api.modules.article.dto;

import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleDto {

    private Long id;

    private String title;

    private String description;

    private Timestamp createdAt;

    private UserDto author;
}
