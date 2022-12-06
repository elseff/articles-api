package com.elseff.project.service.article;

import com.elseff.project.dto.article.ArticleAllFieldsDto;
import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.dto.article.ArticleFieldsCanBeNullDto;
import com.elseff.project.entity.Article;
import com.elseff.project.entity.User;
import com.elseff.project.exception.article.ArticleNotFoundException;
import com.elseff.project.repository.ArticleRepository;
import com.elseff.project.service.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleService {

    private final ArticleRepository repository;

    private final ModelMapper modelMapper;

    public ArticleService(ArticleRepository articleRepository, ModelMapper modelMapper) {
        this.repository = articleRepository;
        this.modelMapper = modelMapper;
    }

    public List<ArticleDto> getAllArticles() {
        return repository.findAll()
                .stream()
                .map(article -> modelMapper.map(article, ArticleDto.class))
                .collect(Collectors.toList());
    }

    public ArticleAllFieldsDto findById(Long id) {
        if (repository.existsById(id)) {
            return modelMapper.map(repository.findById(id).get(), ArticleAllFieldsDto.class);
        } else {
            throw new ArticleNotFoundException(id);
        }
    }

    public void deleteArticleById(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ArticleNotFoundException(id);
        }
    }

    public ArticleAllFieldsDto addArticle(ArticleDto articleDto) {
        User authUser = AuthService.getCurrentUser();
        Article article = modelMapper.map(articleDto, Article.class);
        article.setDate(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));
        article.setAuthor(authUser);
        Article articleFromDb = repository.save(article);

        return modelMapper.map(articleFromDb, ArticleAllFieldsDto.class);
    }

    public ArticleDto updateArticle(Long id, ArticleFieldsCanBeNullDto articleDto) {
        if (repository.existsById(id)) {
            Article articleFromDb = repository.getById(id);
            if (articleDto.getTitle() != null) articleFromDb.setTitle(articleDto.getTitle());
            if (articleDto.getDescription() != null) articleFromDb.setDescription(articleDto.getDescription());
            repository.save(articleFromDb);

            return modelMapper.map(articleFromDb, ArticleDto.class);
        } else {
            throw new ArticleNotFoundException(id);
        }
    }

}
