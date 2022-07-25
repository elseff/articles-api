package com.elseff.project.dto.article;

import lombok.*;

import javax.validation.constraints.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    @Positive(message = "id should be a greater than 0")
    private Long id;

    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    @NotNull(message = "title shouldn't be a null")
    @NotEmpty(message = "title shouldn't be a empty")
    private String title;

    @NotNull(message = "description shouldn't be a null")
    @Size(min = 10, max = 10000, message = "title should be between 10 and 10000 characters")
    @NotEmpty(message = "description shouldn't be a empty")
    private String description;

    private String date;
}
