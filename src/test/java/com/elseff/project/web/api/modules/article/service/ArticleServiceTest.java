package com.elseff.project.web.api.modules.article.service;

import com.elseff.project.persistense.Article;
import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.ArticleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.security.UserDetailsImpl;
import com.elseff.project.web.api.modules.article.dto.ArticleCreationRequest;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.article.dto.ArticleUpdateRequest;
import com.elseff.project.web.api.modules.article.dto.mapper.ArticleDtoMapper;
import com.elseff.project.web.api.modules.article.exception.ArticleNotFoundException;
import com.elseff.project.web.api.modules.article.exception.SomeoneElseArticleException;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class ArticleServiceTest {

    @InjectMocks
    ArticleService articleService;

    @Mock
    ArticleRepository articleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ArticleDtoMapper articleDtoMapper;

    @Mock
    SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Find all articles")
    void findAllArticles() {
        given(articleDtoMapper.mapListArticleEntityToDto(any())).willReturn(List.of(
                new ArticleDto(),
                new ArticleDto(),
                new ArticleDto()
        ));
        given(articleRepository.findAll()).willReturn(Arrays.asList(
                new Article(),
                new Article(),
                new Article()
        ));

        List<ArticleDto> allArticles = articleService.findAll();

        int expectedListSize = 3;
        int actualListSize = allArticles.size();

        Assertions.assertEquals(expectedListSize, actualListSize);

        verify(articleRepository, times(1)).findAll();
        verify(articleDtoMapper, times(1)).mapListArticleEntityToDto(any());
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
    }

    @Test
    void findAllByAuthorId() {
        given(articleRepository.findAllByAuthorId(anyLong())).willReturn(List.of(
                new Article(),
                new Article(),
                new Article()
        ));
        given(articleDtoMapper.mapListArticleEntityToDto(any())).willReturn(List.of(
                new ArticleDto(),
                new ArticleDto(),
                new ArticleDto()
        ));

        List<ArticleDto> articledByAuthorId = articleService.findAllByAuthorId(1L);

        int expectedListSize = 3;
        int actualListSize = articledByAuthorId.size();

        Assertions.assertEquals(expectedListSize, actualListSize);

        verify(articleRepository, times(1)).findAllByAuthorId(anyLong());
        verify(articleDtoMapper, times(1)).mapListArticleEntityToDto(any());
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
    }

    @Test
    @DisplayName("Find article")
    void findById() {
        Article articleFromDb = new Article();

        given(articleRepository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(articleDtoMapper.mapArticleEntityToDto(any(Article.class))).willReturn(new ArticleDto());

        ArticleDto article = articleService.findById(1L);
        Assertions.assertNotNull(article);

        verify(articleRepository, times(1)).findById(anyLong());
        verify(articleDtoMapper, times(1)).mapArticleEntityToDto(any(Article.class));
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
    }

    @Test
    @DisplayName("Find article if article is not found")
    void findById_If_Article_Is_Not_Found() {
        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> articleService.findById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
    }

    @Test
    @DisplayName("Delete article by admin")
    void deleteArticleById_If_Current_User_Is_Admin() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetails user = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(true);
        Article article = Article.builder()
                .id(1L)
                .title("test")
                .description("test")
                .author(getUserEntity())
                .build();
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));
        willDoNothing().given(articleRepository).deleteById(anyLong());

        articleService.deleteArticleById(1L);

        verify(articleRepository, times(1)).deleteById(anyLong());
        verify(articleRepository, times(1)).findById(anyLong());
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(securityUtils);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete article if is it someone else's")
    void deleteArticleById_If_Someone_Else_Article() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(false);
        Article article = Article.builder()
                .id(1L)
                .title("test")
                .description("test")
                .author(getDifferentUserEntity())
                .build();
        given(articleRepository.findById(anyLong()))
                .willReturn(Optional.of(article));

        SomeoneElseArticleException exception = Assertions.assertThrows(SomeoneElseArticleException.class, () -> articleService.deleteArticleById(1L));

        String expectedMessage = "It's someone else's article. You can't modify her";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(securityUtils);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete article if article is not found")
    void deleteArticleById_If_Article_Does_Not_Exists() {
        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> articleService.deleteArticleById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
    }

    @Test
    @DisplayName("Add article")
    void addArticle() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(getUserDetails());
        given(articleRepository.save(any(Article.class))).willReturn(new Article());
        given(articleDtoMapper.mapArticleEntityToDto(any(Article.class))).willReturn(new ArticleDto());
        given(userRepository.getByEmail(anyString())).willReturn(new User());

        ArticleCreationRequest article = ArticleCreationRequest.builder()
                .title("Test Title")
                .description("Test Description")
                .build();

        ArticleDto addedArticle = articleService.addArticle(article);

        Assertions.assertNotNull(addedArticle);

        verify(articleRepository, times(1)).save(any(Article.class));
        verify(articleDtoMapper, times(1)).mapArticleEntityToDto(any(Article.class));
        verify(userRepository, times(1)).getByEmail(anyString());
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
        verifyNoMoreInteractions(userRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article")
    void updateArticle() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        User user = getUserEntity();
        Article article = Article.builder()
                .author(user)
                .title("test")
                .edited(false)
                .build();
        ArticleDto articleDto = ArticleDto.builder()
                .title("test1")
                .build();
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .title("test1")
                .build();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(getUserDetails());
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));
        given(articleRepository.save(article)).willReturn(article);
        given(articleDtoMapper.mapArticleEntityToDto(any(Article.class))).willReturn(articleDto);

        ArticleDto updatedArticle = articleService.updateArticle(1L, articleUpdateRequest);

        String expectedTitle = "test1";
        String actualTitle = updatedArticle.getTitle();
        Assertions.assertEquals(expectedTitle, actualTitle);

        verify(articleRepository, times(1)).findById(anyLong());
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(articleDtoMapper, times(1)).mapArticleEntityToDto(any(Article.class));
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article if is it someone else's")
    void updateArticle_If_Article_Is_Someone_Else() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();
        User author = User.builder()
                .email("author@author.com")
                .build();
        Article article = Article.builder()
                .author(author)
                .title("test")
                .build();

        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .title("test1")
                .build();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));

        SomeoneElseArticleException exception =
                Assertions.assertThrows(SomeoneElseArticleException.class, () -> articleService.updateArticle(1L, articleUpdateRequest));

        String expectedMessage = "It's someone else's article. You can't modify her";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article if article is not found")
    void updateArticle_If_Article_Is_Not_Found() {
        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .title("test1")
                .build();

        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException =
                Assertions.assertThrows(ArticleNotFoundException.class, () -> articleService.updateArticle(1L, articleUpdateRequest));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
    }

    @NotNull
    private UserDetailsImpl getUserDetails() {
        return UserDetailsImpl.builder()
                .email("test@test.com")
                .password("test")
                .grantedAuthorities(Set.of(getRoleUser(), getRoleAdmin()))
                .build();
    }

    @NotNull
    private User getUserEntity() {
        return User.builder()
                .id(1L)
                .firstName("test")
                .lastName("test")
                .email("test@test.com")
                .country("test")
                .password("test")
                .roles(Set.of(getRoleUser(), getRoleAdmin()))
                .build();
    }

    @NotNull
    private User getDifferentUserEntity() {
        return User.builder()
                .id(2L)
                .firstName("testt")
                .lastName("testt")
                .email("test1@test.com")
                .country("testt")
                .password("testt")
                .roles(Set.of(getRoleUser()))
                .build();
    }

    private Role getRoleAdmin() {
        return new Role("ROLE_ADMIN");
    }

    private Role getRoleUser() {
        return new Role("ROLE_USER");
    }
}