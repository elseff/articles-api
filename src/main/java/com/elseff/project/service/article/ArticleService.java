package com.elseff.project.service.article;

import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.entity.Article;
import com.elseff.project.exception.article.ArticleNotFoundException;
import com.elseff.project.repository.ArticleRepository;
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

    public ArticleDto findById(Long id) {
        if (repository.existsById(id)) {
            return modelMapper.map(repository.findById(id).get(), ArticleDto.class);
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

    public ArticleDto addArticle(ArticleDto articleDto) {
        Article article = modelMapper.map(articleDto, Article.class);
        article.setDate(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date()));
        Article articleFromDb = repository.save(article);
        return modelMapper.map(articleFromDb, ArticleDto.class);
    }

    public ArticleDto updateArticle(Long id, ArticleDto articleDto) {
        if (repository.existsById(id)) {
            Article articleFromDb = repository.getById(id);
            if (articleDto.getTitle() != null) articleFromDb.setTitle(articleDto.getTitle());
            if (articleDto.getDescription() != null) articleFromDb.setDescription(articleDto.getDescription());
            if (articleDto.getDate() != null) articleFromDb.setDate(articleFromDb.getDate());
            repository.save(articleFromDb);
            return modelMapper.map(articleFromDb, ArticleDto.class);
        } else {
            throw new ArticleNotFoundException(id);
        }
    }

}
