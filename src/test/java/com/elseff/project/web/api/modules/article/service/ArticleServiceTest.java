package com.elseff.project.web.api.modules.article.service;

import com.elseff.project.persistense.Article;
import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.ArticleRepository;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.UserDetailsImpl;
import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsCanBeNullDto;
import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsDto;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
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
import org.modelmapper.ModelMapper;
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
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get all articles")
    void getAllArticles() {
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
        verifyNoMoreInteractions(articleRepository);
    }

    @Test
    @DisplayName("Get article")
    void findById() {
        Article articleFromDb = new Article();

        given(articleRepository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(modelMapper.map(articleFromDb, ArticleAllFieldsDto.class)).willReturn(new ArticleAllFieldsDto());

        ArticleAllFieldsDto article = service.findById(1L);
        Assertions.assertNotNull(article);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
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
        given(roleRepository.getByName("ROLE_ADMIN")).willReturn(getRoleAdmin());
        given(roleRepository.getByName("ROLE_USER")).willReturn(getRoleUser());
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(new Article(1L, "test", "test", null, getUserEntity())));
        willDoNothing().given(articleRepository).deleteById(anyLong());

        service.deleteArticleById(1L);

        verify(articleRepository, times(1)).deleteById(anyLong());
        verify(articleRepository, times(1)).findById(anyLong());
        verify(roleRepository, times(1)).getByName(anyString());
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(roleRepository);
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
        given(roleRepository.getByName("ROLE_USER")).willReturn(getRoleUser());
        given(roleRepository.getByName("ROLE_ADMIN")).willReturn(getRoleAdmin());
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(new Article(1L, "test", "test", null, getDifferentUserEntity())));
        user.setGrantedAuthorities(Set.of(roleRepository.getByName("ROLE_USER")));

        SomeoneElseArticleException exception = Assertions.assertThrows(SomeoneElseArticleException.class, () -> service.deleteArticleById(1L));

        String expectedMessage = "It's someone else's article. You can't modify her";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verify(roleRepository, times(2)).getByName(anyString());
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(roleRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete article if article is not found")
    void deleteArticleById_If_Article_Does_Not_Exists() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException = Assertions.assertThrows(ArticleNotFoundException.class, () -> service.deleteArticleById(1L));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
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

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(getUserDetails());
        given(articleRepository.save(any(Article.class))).willReturn(article);
        given(modelMapper.map(articleDto, Article.class)).willReturn(article);
        given(modelMapper.map(article, ArticleAllFieldsDto.class)).willReturn(articleAllFieldsDto);
        given(userRepository.getByEmail(anyString())).willReturn(new User());

        ArticleAllFieldsDto addedArticle = service.addArticle(articleDto);

        Assertions.assertNotNull(addedArticle);

        verify(articleRepository, times(1)).save(any(Article.class));
        verify(modelMapper, times(1)).map(articleDto, Article.class);
        verify(modelMapper, times(1)).map(article, ArticleAllFieldsDto.class);
        verifyNoMoreInteractions(articleRepository);
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

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(getUserDetails());
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(articleFromDb));
        given(articleRepository.save(articleFromDb)).willReturn(articleFromDb);
        given(modelMapper.map(articleFromDb, ArticleDto.class)).willReturn(articleDto);

        ArticleDto updatedArticle = service.updateArticle(1L, articleAllFieldsCanBeNullDto);

        String expectedTitle = "test1";
        String actualTitle = updatedArticle.getTitle();
        Assertions.assertEquals(expectedTitle, actualTitle);

        verify(articleRepository, times(1)).findById(anyLong());
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(modelMapper, times(1)).map(articleFromDb, ArticleDto.class);
        verifyNoMoreInteractions(articleRepository);
        verifyNoMoreInteractions(modelMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update article if is it someone else's")
    void updateArticle_If_Article_Is_Someone_Else() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();
        Article articleFromDb = new Article();
        User author = new User();
        author.setEmail("author@author.com");
        articleFromDb.setAuthor(author);
        articleFromDb.setTitle("test");
        ArticleAllFieldsCanBeNullDto articleDto = new ArticleAllFieldsCanBeNullDto();
        articleDto.setTitle("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(articleFromDb));

        SomeoneElseArticleException exception =
                Assertions.assertThrows(SomeoneElseArticleException.class, () -> service.updateArticle(1L, articleDto));

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
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();
        ArticleAllFieldsCanBeNullDto articleDto = new ArticleAllFieldsCanBeNullDto();
        articleDto.setTitle("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        ArticleNotFoundException articleNotFoundException =
                Assertions.assertThrows(ArticleNotFoundException.class, () -> service.updateArticle(1L, articleDto));

        String expectedMessage = "could not found article 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(articleRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(articleRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
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