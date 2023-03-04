package com.elseff.project.web.api.modules.article.dto.mapper;

import com.elseff.project.persistense.ArticleEntity;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleDtoMapper {

    public ArticleDto mapArticleEntityToDto(ArticleEntity article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .createdAt(article.getCreatedAt())
                .edited(article.getEdited())
                .updatedAt(article.getUpdatedAt())
                .author(UserDto.builder()
                        .id(article.getAuthor().getId())
                        .firstName(article.getAuthor().getFirstName())
                        .lastName(article.getAuthor().getLastName())
                        .build())
                .build();
    }

    public ArticleDto mapArticleEntityToSimpleDto(ArticleEntity article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .description(article.getDescription())
                .build();
    }

    public List<ArticleDto> mapListArticleEntityToDto(List<ArticleEntity> articles) {
        return articles.stream()
                .map(this::mapArticleEntityToDto)
                .collect(Collectors.toList());
    }

    public List<ArticleDto> mapListArticleEntityToSimpleDto(List<ArticleEntity> articles) {
        return articles.stream()
                .map(this::mapArticleEntityToSimpleDto)
                .collect(Collectors.toList());
    }
}
