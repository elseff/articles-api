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
import lombok.Cleanup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.time.Instant;
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
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleDtoMapper articleDtoMapper;

    @Mock
    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get all articles")
    void getAllArticles() {
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

        List<ArticleDto> allArticles = service.getAllArticles();

        int expectedListSize = 3;
        int actualListSize = allArticles.size();

        Assertions.assertEquals(expectedListSize, actualListSize);

        verify(articleRepository, times(1)).findAll();
        verify(articleDtoMapper, times(1)).mapListArticleEntityToDto(any());
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
    }

    @Test
    @DisplayName("Get article")
    void findById() {
        Article articleFromDb = new Article();

        given(articleRepository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(articleDtoMapper.mapArticleEntityToDto(any(Article.class))).willReturn(new ArticleDto());

        ArticleDto article = service.findById(1L);
        Assertions.assertNotNull(article);

        verify(articleRepository, times(1)).findById(anyLong());
        verify(articleDtoMapper, times(1)).mapArticleEntityToDto(any(Article.class));
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(articleDtoMapper);
    }

    @Test
    @DisplayName("Get article if article is not found")
    void findById_If_Article_Is_Not_Found() {
        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.findById(1L));

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
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(new Article(1L, "test", "test", null, getUserEntity())));
        willDoNothing().given(articleRepository).deleteById(anyLong());

        service.deleteArticleById(1L);

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
        given(articleRepository.findById(anyLong()))
                .willReturn(Optional.of(new Article(1L, "test", "test", null, getDifferentUserEntity())));

        SomeoneElseArticleException exception = Assertions.assertThrows(SomeoneElseArticleException.class, () -> service.deleteArticleById(1L));

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

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.deleteArticleById(1L));

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

        ArticleDto addedArticle = service.addArticle(article);

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
                .build();
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("test1");

        ArticleUpdateRequest articleUpdateRequest = ArticleUpdateRequest.builder()
                .title("test1")
                .build();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(getUserDetails());
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));
        given(articleRepository.save(article)).willReturn(article);
        given(articleDtoMapper.mapArticleEntityToDto(any(Article.class))).willReturn(articleDto);

        ArticleDto updatedArticle = service.updateArticle(1L, articleUpdateRequest);

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
                Assertions.assertThrows(SomeoneElseArticleException.class, () -> service.updateArticle(1L, articleUpdateRequest));

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
                Assertions.assertThrows(ArticleNotFoundException.class, () -> service.updateArticle(1L, articleUpdateRequest));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
    }

    @NotNull
    private UserDetailsImpl getUserDetails() {
        return new UserDetailsImpl(
                "test@test.com",
                "test",
                Set.of(getRoleUser(), getRoleAdmin())
        );
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
                Timestamp.from(Instant.now()),
                Set.of(getRoleUser(), getRoleAdmin()),
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
                Timestamp.from(Instant.now()),
                Set.of(getRoleUser()),
                List.of()
        );
        return value;
    }

    private Role getRoleAdmin() {
        return new Role("ROLE_ADMIN");
    }

    private Role getRoleUser() {
        return new Role("ROLE_USER");
    }
}