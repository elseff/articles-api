package com.elseff.project.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAllFieldsCanBeNullDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    private String title;

    @Size(min = 10, max = 10000, message = "description should be between 10 and 10000 characters")
    private String description;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String date;
}
