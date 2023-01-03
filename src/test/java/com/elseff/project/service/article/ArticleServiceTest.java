package com.elseff.project.service.article;

import com.elseff.project.dto.article.ArticleAllFieldsDto;
import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.dto.article.ArticleAllFieldsCanBeNullDto;
import com.elseff.project.entity.Article;
import com.elseff.project.entity.User;
import com.elseff.project.enums.Role;
import com.elseff.project.exception.article.ArticleNotFoundException;
import com.elseff.project.exception.article.SomeoneElseArticleException;
import com.elseff.project.repository.ArticleRepository;
import com.elseff.project.service.auth.AuthService;
import lombok.Cleanup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

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
    @DisplayName("Get all articles")
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
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Get article")
    void findById() {
        Article articleFromDb = new Article();

        given(repository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(modelMapper.map(articleFromDb, ArticleAllFieldsDto.class)).willReturn(new ArticleAllFieldsDto());

        ArticleAllFieldsDto article = service.findById(1L);
        Assertions.assertNotNull(article);

        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Get article if article is not found")
    void findById_If_Article_Is_Not_Found() {
        given(repository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.findById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Delete article by admin")
    void deleteArticleById_If_Current_User_Is_Admin() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();

        given(repository.findById(anyLong())).willReturn(Optional.of(new Article(1L, "test", "test", null, user)));
        willDoNothing().given(repository).deleteById(anyLong());
        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);

        service.deleteArticleById(1L);

        verify(repository, times(1)).deleteById(anyLong());
        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete article if is it someone else's")
    void deleteArticleById_If_Someone_Else_Article() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();
        user.setRoles(Set.of(Role.USER));

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(repository.findById(anyLong())).willReturn(Optional.of(new Article(1L, "test", "test", null, getDifferentUserEntity())));

        SomeoneElseArticleException exception = Assertions.assertThrows(SomeoneElseArticleException.class, () -> service.deleteArticleById(1L));

        String expectedMessage = "It's someone else's article. You can't modify her";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete article if article is not found")
    void deleteArticleById_If_Article_Does_Not_Exists() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(repository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.deleteArticleById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Add article")
    void addArticle() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        Article article = new Article();
        ArticleDto articleDto = new ArticleDto();
        ArticleAllFieldsDto articleAllFieldsDto = new ArticleAllFieldsDto();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(getUserEntity());
        given(repository.save(any(Article.class))).willReturn(article);
        given(modelMapper.map(articleDto, Article.class)).willReturn(article);
        given(modelMapper.map(article, ArticleAllFieldsDto.class)).willReturn(articleAllFieldsDto);

        ArticleAllFieldsDto addedArticle = service.addArticle(articleDto);

        Assertions.assertNotNull(addedArticle);

        verify(repository, times(1)).save(any(Article.class));
        verify(modelMapper, times(1)).map(articleDto, Article.class);
        verify(modelMapper, times(1)).map(article, ArticleAllFieldsDto.class);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(modelMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article")
    void updateArticle() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();
        Article articleFromDb = new Article();
        articleFromDb.setAuthor(user);
        articleFromDb.setTitle("test");
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("test1");
        ArticleAllFieldsCanBeNullDto articleAllFieldsCanBeNullDto = new ArticleAllFieldsCanBeNullDto();
        articleAllFieldsCanBeNullDto.setTitle("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(repository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(repository.save(articleFromDb)).willReturn(articleFromDb);
        given(modelMapper.map(articleFromDb, ArticleDto.class)).willReturn(articleDto);

        ArticleDto updatedArticle = service.updateArticle(1L, articleAllFieldsCanBeNullDto);

        String expectedTitle = "test1";
        String actualTitle = updatedArticle.getTitle();
        Assertions.assertEquals(expectedTitle, actualTitle);

        verify(repository, times(1)).findById(anyLong());
        verify(repository, times(1)).save(any(Article.class));
        verify(modelMapper, times(1)).map(articleFromDb, ArticleDto.class);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(modelMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article if is it someone else's")
    void updateArticle_If_Article_Is_Someone_Else() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();
        Article articleFromDb = new Article();
        articleFromDb.setAuthor(new User());
        articleFromDb.setTitle("test");
        ArticleAllFieldsCanBeNullDto articleDto = new ArticleAllFieldsCanBeNullDto();
        articleDto.setTitle("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(repository.findById(anyLong())).willReturn(Optional.of(articleFromDb));

        SomeoneElseArticleException exception =
                Assertions.assertThrows(SomeoneElseArticleException.class, () -> service.updateArticle(1L, articleDto));

        String expectedMessage = "It's someone else's article. You can't modify her";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article if article is not found")
    void updateArticle_If_Article_Is_Not_Found() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();
        ArticleAllFieldsCanBeNullDto articleDto = new ArticleAllFieldsCanBeNullDto();
        articleDto.setTitle("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(repository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException =
                Assertions.assertThrows(ArticleNotFoundException.class, () -> service.updateArticle(1L, articleDto));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(repository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @NotNull
    private User getUserEntity() {
        User value = new User(
                1L,
                "test",
                "test",
                "test@test.com",
                "test",
                "test",
                Set.of(Role.USER, Role.ADMIN),
                List.of()
        );
        return value;
    }

    @NotNull
    private User getDifferentUserEntity() {
        User value = new User(
                2L,
                "testt",
                "testt",
                "test1@test.com",
                "testt",
                "testt",
                Set.of(Role.USER),
                List.of()
        );
        return value;
    }
}