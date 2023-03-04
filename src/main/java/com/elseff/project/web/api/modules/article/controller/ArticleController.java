package com.elseff.project.web.api.modules.article.controller;

import com.elseff.project.persistense.Article;
import com.elseff.project.web.api.modules.article.dto.ArticleCreationRequest;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.article.dto.ArticleUpdateRequest;
import com.elseff.project.web.api.modules.article.dto.mapper.ArticleDtoMapper;
import com.elseff.project.web.api.modules.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Article controller", description = "Article management")
public class ArticleController {

    ArticleService articleService;
    ArticleDtoMapper articleDtoMapper;

    @Operation(summary = "Get all articles",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ArticleDto.class))
                    ),
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ArticleDto> findAll(@Parameter(description = "author id")
                                    @RequestParam(required = false, name = "authorId") Long authorId) {
        List<Article> articles = authorId == null
                ? articleService.findAll()
                : articleService.findAllByAuthorId(authorId);

        return articleDtoMapper.mapListArticleEntityToDto(articles);
    }

    @Operation(summary = "Get specific article by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ArticleDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Article not found", content = @Content),
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ArticleDto findById(@Parameter(description = "Article id", required = true)
                               @PathVariable Long id) {
        Article article = articleService.findById(id);

        return articleDtoMapper.mapArticleEntityToDto(article);
    }

    @Operation(summary = "Add new article",
            description = "Add new article. The author will be the current authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Article has been successfully added",
                            content = @Content(schema = @Schema(implementation = ArticleDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Article not valid", content = @Content),
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDto addArticle(@Parameter(description = "Article creation request", required = true)
                                 @RequestBody @Valid ArticleCreationRequest articleCreationRequest) {
        Article article = articleService.addArticle(articleCreationRequest);

        return articleDtoMapper.mapArticleEntityToDto(article);
    }

    @Operation(summary = "Delete article by id",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Article has been successfully deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Article not found", content = @Content),
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@Parameter(description = "Article id", required = true)
                              @PathVariable Long id) {
        articleService.deleteArticleById(id);
    }

    @Operation(summary = "Update article by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Article has been successfully updated",
                            content = @Content(schema = @Schema(implementation = ArticleDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Article not valid", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Article not found", content = @Content),
            }
    )
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ArticleDto updateArticle(@Parameter(description = "Article id", required = true)
                                    @PathVariable
                                            Long id,
                                    @Parameter(description = "Article update request", required = true)
                                    @RequestBody @Valid ArticleUpdateRequest updateRequest) {
        Article article = articleService.updateArticle(id, updateRequest);

        return articleDtoMapper.mapArticleEntityToDto(article);
    }
}
