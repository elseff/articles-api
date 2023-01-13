package com.elseff.project.web.api.modules.article.service;

import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsDto;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsCanBeNullDto;
import com.elseff.project.persistense.Article;
import com.elseff.project.persistense.User;
import com.elseff.project.security.Role;
import com.elseff.project.web.api.modules.article.exception.ArticleNotFoundException;
import com.elseff.project.web.api.modules.article.exception.SomeoneElseArticleException;
import com.elseff.project.persistense.dao.ArticleRepository;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    public ArticleService(ArticleRepository articleRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    public List<ArticleDto> getAllArticles() {
        return articleRepository.findAll()
                .stream()
                .map(article -> modelMapper.map(article, ArticleDto.class))
                .collect(Collectors.toList());
    }

    public ArticleAllFieldsDto findById(Long id) {
        return modelMapper.map(articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id)), ArticleAllFieldsDto.class);
    }

    public void deleteArticleById(Long id) {
        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        Article articleFromDb = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        if (currentUser.getAuthorities().contains(Role.ADMIN)) {
            log.info("delete article {} by admin {}", id, currentUser.getUsername());
            articleRepository.deleteById(id);
        } else {
            if (articleFromDb.getAuthor().getEmail().equals(currentUser.getUsername())) {
                log.info("delete article {} by user {}", id, currentUser.getUsername());
                articleRepository.deleteById(id);
            } else throw new SomeoneElseArticleException();
        }
    }

    public ArticleAllFieldsDto addArticle(ArticleDto articleDto) {
        UserDetails authUser = Objects.requireNonNull(AuthService.getCurrentUser());

        User author = userRepository.getByEmail(authUser.getUsername());

        Article article = modelMapper.map(articleDto, Article.class);
        article.setDate(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));
        article.setAuthor(author);
        Article articleFromDb = articleRepository.save(article);

        return modelMapper.map(articleFromDb, ArticleAllFieldsDto.class);
    }

    public ArticleDto updateArticle(Long id, ArticleAllFieldsCanBeNullDto articleDto) {
        UserDetails authUser = Objects.requireNonNull(AuthService.getCurrentUser());

        Article articleFromDb = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        if (articleFromDb.getAuthor().getEmail().equals(authUser.getUsername())) {
            if (articleDto.getTitle() != null) articleFromDb.setTitle(articleDto.getTitle());
            if (articleDto.getDescription() != null) articleFromDb.setDescription(articleDto.getDescription());
            articleRepository.save(articleFromDb);
            log.info("updated article {} by user {}", articleFromDb.getId(), authUser.getUsername());
            return modelMapper.map(articleFromDb, ArticleDto.class);
        } else throw new SomeoneElseArticleException();
    }

}
