package com.elseff.project.web.api.modules.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Article creation request")
public class ArticleCreationRequest {

    @NotNull(message = "Title shouldn't be a null")
    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    String title;

    @NotNull(message = "Description shouldn't be a null")
    @Size(min = 10, max = 10000, message = "description should be between 10 and 10000 characters")
    String description;
}
