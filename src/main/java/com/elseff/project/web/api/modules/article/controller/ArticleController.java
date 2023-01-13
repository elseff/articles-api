package com.elseff.project.web.api.modules.article.controller;

import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsDto;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsCanBeNullDto;
import com.elseff.project.web.api.modules.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "Article controller", description = "Article management")
@CrossOrigin(origins = {"http://192.168.100.4:4200", "http://localhost:4200"})
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "Get all articles",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ArticleAllFieldsDto.class))
                    ),
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ArticleDto> getArticles() {
        return articleService.getAllArticles();
    }

    @Operation(summary = "Get specific article by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ArticleAllFieldsDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Article not found", content = @Content),
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ArticleAllFieldsDto getSpecific(@Parameter(description = "Article id")
                                           @PathVariable Long id) {
        return articleService.findById(id);
    }

    @Operation(summary = "Add new article",
            description = "Add new article. The author will be the current authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Article has been successfully added",
                            content = @Content(schema = @Schema(implementation = ArticleAllFieldsDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Article not valid", content = @Content),
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleAllFieldsDto addArticle(@Parameter(description = "Article title and description")
                                          @RequestBody @Valid ArticleDto articleDto) {
        return articleService.addArticle(articleDto);
    }

    @Operation(summary = "Delete article by id",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Article has been successfully deleted", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Article not found", content = @Content),
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@Parameter(description = "Article id")
                              @PathVariable Long id) {
        articleService.deleteArticleById(id);
    }

    @Operation(summary = "Update article by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Article has been successfully updated",
                            content = @Content(schema = @Schema(implementation = ArticleAllFieldsCanBeNullDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Article not valid", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Article not found", content = @Content),
            }
    )
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ArticleDto updateArticle(@Parameter(description = "Updated article")
                                    @RequestBody
                                    @Valid
                                            ArticleAllFieldsCanBeNullDto articleDto,
                                    @Parameter(description = "Article id")
                                    @PathVariable
                                            Long id) {
        return articleService.updateArticle(id, articleDto);
    }
}
