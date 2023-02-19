package com.elseff.project.web.api.modules.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Size;


@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Article update request")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleUpdateRequest {

    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    String title;

    @Size(min = 10, max = 10000, message = "description should be between 10 and 10000 characters")
    String description;
}
