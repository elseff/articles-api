package com.elseff.project.dto.article;

import com.elseff.project.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ArticleAllFieldsDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "title shouldn't be a null")
    @Size(min = 10, max = 120, message = "title should be between 10 and 120 characters")
    private String title;

    @NotNull(message = "description shouldn't be a null")
    @Size(min = 10, max = 10000, message = "description should be between 10 and 10000 characters")
    private String description;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String date;

    @Schema(description = "Author is the current authenticated user", accessMode = Schema.AccessMode.READ_ONLY)
    private UserDto author;
}
