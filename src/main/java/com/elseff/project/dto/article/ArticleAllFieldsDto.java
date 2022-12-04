package com.elseff.project.dto.article;

import com.elseff.project.dto.user.UserDto;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAllFieldsDto {

    private Long id;

    @NotNull(message = "title shouldn't be a null")
    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    private String title;

    @NotNull(message = "description shouldn't be a null")
    @Size(min = 10, max = 10000, message = "description should be between 10 and 10000 characters")
    private String description;

    private String date;

    private UserDto author;
}
