package com.elseff.project.web.api.modules.user.dto;

import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAllFieldsDto {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "firstname shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "firstname should be valid")
    @Size(min = 2, max = 40, message = "firstname size should be between 2 and 40")
    private String firstName;

    @NotNull(message = "lastname shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "lastname should be valid")
    @Size(min = 2, max = 40, message = "lastname size should be between 2 and 40")
    private String lastName;

    @Email(message = "email should be valid")
    @NotNull(message = "email shouldn't be a null")
    private String email;

    @Size(min = 3, max = 74)
    @NotNull(message = "country shouldn't be a null")
    @Pattern(regexp = "([A-Z][a-zA-Z]*)", message = "country should be valid")
    private String country;

    @NotNull(message = "password shouldn't be a null")
    @Size(min = 4, message = "password size should be greater than 4")
    private String password;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<ArticleDto> articles;
}
