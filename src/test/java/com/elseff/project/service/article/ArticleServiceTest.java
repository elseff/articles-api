package com.elseff.project.service.article;

import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.entity.Article;
import com.elseff.project.exception.IdLessThanZeroException;
import com.elseff.project.exception.article.ArticleNotFoundException;
import com.elseff.project.repository.ArticleRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ArticleServiceTest {

    @InjectMocks
    private ArticleService service;

    @Mock
    private ArticleRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllArticles() {
        given(repository.findAll()).willReturn(Arrays.asList(
                new Article(),
                new Article(),
                new Article()
        ));

        List<ArticleDto> allArticles = service.getAllArticles();

        int expectedListSize = 3;
        int actualListSize = allArticles.size();

        Assertions.assertEquals(expectedListSize, actualListSize);

        verify(repository, times(1)).findAll();
    }

    @Test
    void findById_If_Id_Less_Than_Zero() {
        IdLessThanZeroException idLessThanZeroException = Assertions.assertThrows(IdLessThanZeroException.class, () -> service.findById(-1L));

        String expectedMessage = "id must be a greater than 0";
        String actualMessage = idLessThanZeroException.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findById_If_Article_Does_Not_Exists() {
        given(repository.existsById(anyLong())).willReturn(false);

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.findById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).existsById(anyLong());
    }

    @Test
    void findById() {
        Article articleFromDb = new Article();

        given(repository.existsById(anyLong())).willReturn(true);
        given(repository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(modelMapper.map(articleFromDb, ArticleDto.class)).willReturn(new ArticleDto());

        ArticleDto article = service.findById(1L);
        Assertions.assertNotNull(article);

        verify(repository, times(1)).existsById(anyLong());
        verify(repository, times(1)).findById(anyLong());
    }

    @Test
    void deleteArticleById_If_Id_Less_Than_Zero() {
        IdLessThanZeroException idLessThanZeroException = Assertions.assertThrows(IdLessThanZeroException.class, () -> service.deleteArticleById(-1L));

        String expectedMessage = "id must be a greater than 0";
        String actualMessage = idLessThanZeroException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void deleteArticleById_If_Article_Does_Not_Exists() {
        given(repository.existsById(anyLong())).willReturn(false);

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.deleteArticleById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).existsById(anyLong());
    }

    @Test
    void deleteArticleById() {
        given(repository.existsById(anyLong())).willReturn(true);
        willDoNothing().given(repository).deleteById(anyLong());

        service.deleteArticleById(1L);

        verify(repository, times(1)).existsById(anyLong());
        verify(repository, times(1)).deleteById(anyLong());
    }

    @Test
    void addArticle() {
        Article article = new Article();
        ArticleDto articleDto = new ArticleDto();

        given(repository.save(any(Article.class))).willReturn(article);
        given(modelMapper.map(articleDto, Article.class)).willReturn(article);
        given(modelMapper.map(article, ArticleDto.class)).willReturn(articleDto);

        ArticleDto addedArticle = service.addArticle(articleDto);

        Assertions.assertNotNull(addedArticle);

        verify(repository, times(1)).save(any(Article.class));
        verify(modelMapper, times(1)).map(articleDto, Article.class);
        verify(modelMapper, times(1)).map(article, ArticleDto.class);
    }

    @Test
    void updateArticle() {
        Article articleFromDb = new Article();
        articleFromDb.setTitle("test");
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("test1");

        given(repository.getById(anyLong())).willReturn(articleFromDb);
        given(repository.save(articleFromDb)).willReturn(articleFromDb);
        given(modelMapper.map(articleFromDb, ArticleDto.class)).willReturn(articleDto);

        ArticleDto updateArticle = service.updateArticle(1L, articleDto);

        String expectedTitle = "test1";
        String actualTitle = updateArticle.getTitle();
        Assertions.assertEquals(expectedTitle, actualTitle);

        verify(repository, times(1)).getById(anyLong());
        verify(repository, times(1)).save(any(Article.class));
        verify(modelMapper, times(1)).map(articleFromDb, ArticleDto.class);
    }
}