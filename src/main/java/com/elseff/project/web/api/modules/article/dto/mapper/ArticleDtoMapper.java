package com.elseff.project.web.api.modules.article.dto.mapper;

import com.elseff.project.persistense.Article;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleDtoMapper {
    public ArticleDto mapArticleEntityToDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .createdAt(article.getCreatedAt())
                .author(UserDto.builder()
                        .id(article.getAuthor().getId())
                        .firstName(article.getAuthor().getFirstName())
                        .lastName(article.getAuthor().getLastName())
                        .build())
                .build();
    }

    public ArticleDto mapArticleEntityToSimpleDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .build();
    }

    public List<ArticleDto> mapListArticleEntityToDto(List<Article> articles) {
        return articles.stream()
                .map(this::mapArticleEntityToDto)
                .collect(Collectors.toList());
    }

    public List<ArticleDto> mapListArticleEntityToSimpleDto(List<Article> articles) {
        return articles.stream()
                .map(this::mapArticleEntityToSimpleDto)
                .collect(Collectors.toList());
    }
}
