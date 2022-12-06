package com.elseff.project.dto.article;

import lombok.*;

import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFieldsCanBeNullDto {

    private Long id;

    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    private String title;

    @Size(min = 10, max = 10000, message = "description should be between 10 and 10000 characters")
    private String description;

    private String date;
}
