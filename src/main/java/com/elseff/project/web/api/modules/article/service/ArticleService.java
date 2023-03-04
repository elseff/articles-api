package com.elseff.project.web.api.modules.article.service;

import com.elseff.project.persistense.Article;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.ArticleRepository;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.web.api.modules.article.dto.ArticleCreationRequest;
import com.elseff.project.web.api.modules.article.dto.ArticleUpdateRequest;
import com.elseff.project.web.api.modules.article.dto.mapper.ArticleDtoMapper;
import com.elseff.project.web.api.modules.article.exception.ArticleNotFoundException;
import com.elseff.project.web.api.modules.article.exception.SomeoneElseArticleException;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ArticleService {

    ArticleRepository articleRepository;
    RoleRepository roleRepository;
    UserRepository userRepository;

    ArticleDtoMapper articleDtoMapper;
    SecurityUtils securityUtils;

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    public List<Article> findAllByAuthorId(Long authorId) {
        return articleRepository.findAllByAuthorId(authorId);
    }

    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() ->
                        new ArticleNotFoundException(id));
    }

    public void deleteArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());
        boolean currentUserIsAdmin = securityUtils.userIsAdmin(currentUser);

        if (currentUserIsAdmin) {
            log.info("delete article {} by admin {}", id, currentUser.getUsername());
            articleRepository.deleteById(id);
        } else {
            if (article.getAuthor().getEmail().equals(currentUser.getUsername())) {
                log.info("delete article {} by user {}", id, currentUser.getUsername());
                articleRepository.deleteById(id);
            } else throw new SomeoneElseArticleException();
        }
    }

    public Article addArticle(ArticleCreationRequest articleCreationRequest) {
        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        User author = userRepository.getByEmail(currentUser.getUsername());

        Article article = Article.builder()
                .title(articleCreationRequest.getTitle())
                .description(articleCreationRequest.getDescription())
                .author(author)
                .createdAt(Timestamp.from(Instant.now()))
                .build();
        article = articleRepository.save(article);

        return article;
    }

    public Article updateArticle(Long id, ArticleUpdateRequest updateRequest) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        if (article.getAuthor().getEmail().equals(currentUser.getUsername())) {
            if (updateRequest.getTitle() != null)
                article.setTitle(updateRequest.getTitle());
            if (updateRequest.getDescription() != null)
                article.setDescription(updateRequest.getDescription());
            if (article.getEdited() == false)
                article.setEdited(true);

            article.setUpdatedAt(Timestamp.from(Instant.now()));
            article = articleRepository.save(article);
            log.info("updated article {} by user {}", article.getId(), currentUser.getUsername());

            return article;
        } else throw new SomeoneElseArticleException();
    }

}
