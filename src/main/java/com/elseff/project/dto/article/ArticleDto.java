package com.elseff.project.dto.article;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {

    @Positive(message = "id should be a greater than 0")
    private Long id;

    @NotNull(message = "title shouldn't be a null")
    @NotEmpty(message = "title shouldn't be a empty")
    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    private String title;

    @NotNull(message = "description shouldn't be a null")
    @NotEmpty(message = "description shouldn't be a empty")
    @Size(min = 10, max = 10000, message = "title should be between 10 and 10000 characters")
    private String description;

    private String date;
}
