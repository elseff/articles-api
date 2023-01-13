package com.elseff.project.web.api.modules.article.controller;

import com.elseff.project.exception.handling.dto.Violation;
import com.elseff.project.persistense.Article;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.ArticleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.Role;
import com.elseff.project.web.api.modules.article.dto.ArticleAllFieldsDto;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class ArticleControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final String endPoint = "/api/v1/articles";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(getUser());
        userRepository.save(getAdmin());
        articleRepository.deleteAll();
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        Assertions.assertNotNull(articleRepository);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Get all Articles")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getArticles() throws Exception {
        User currentAuthenticatedUser = userRepository.getByEmail(getUser().getEmail());
        articleRepository.save(getArticle(currentAuthenticatedUser));
        articleRepository.save(getArticle(currentAuthenticatedUser));
        articleRepository.save(getArticle(currentAuthenticatedUser));

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<ArticleDto> listArticles = this.objectMapper.readValue(response, new TypeReference<>() {
        });

        int expectedListSize = 3;
        int actualListSize = listArticles.size();

        Assertions.assertNotNull(listArticles);
        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @DisplayName("Get article")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getArticleById() throws Exception {
        User currentAuthenticatedUser = userRepository.getByEmail(getUser().getEmail());
        Article articleFromDb = articleRepository.save(getArticle(currentAuthenticatedUser));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ArticleDto article = objectMapper.readValue(response, ArticleDto.class);

        String expectedArticleTitle = "test article";
        String actualArticleTitle = article.getTitle();

        Assertions.assertNotNull(article);
        Assertions.assertEquals(expectedArticleTitle, actualArticleTitle);
    }

    @Test
    @DisplayName("Get article if article is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getArticleById_If_Article_Is_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Add article")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addArticle() throws Exception {
        ArticleDto contentArticle = new ArticleDto(
                null,
                "test add new article title",
                "test add new article description",
                null);

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ArticleAllFieldsDto responseArticle = objectMapper.readValue(response, ArticleAllFieldsDto.class);

        String expectedArticleTitle = "test add new article title";
        String actualArticleTitle = responseArticle.getTitle();
        String expectedAuthorFirstName = "user";
        String actualAuthorFirstName = responseArticle.getAuthor().getFirstName();

        Assertions.assertNotNull(responseArticle);
        Assertions.assertEquals(expectedArticleTitle, actualArticleTitle);
        Assertions.assertEquals(expectedAuthorFirstName, actualAuthorFirstName);
    }

    @Test
    @DisplayName("Add article if article is not valid")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void addArticle_If_Article_Is_Not_Valid() throws Exception {
        ArticleDto contentArticle = new ArticleDto(null, "test", "test", "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //remove first 14 characters to get a list from string
        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedStringViolations = new ArrayList<>(List.of(
                "title should be between 10 and 120 characters",
                "description should be between 10 and 10000 characters"
        ));
        expectedStringViolations.sort(Comparator.naturalOrder());

        List<String> actualStringViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedStringViolations, actualStringViolations);
    }

    @Test
    @DisplayName("Delete article by user")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteArticle_By_User() throws Exception {
        UserDetails currentAuthenticatedUser = AuthService.getCurrentUser();
        User author = userRepository.getByEmail(currentAuthenticatedUser.getUsername());
        Article articleFromDb = articleRepository.save(getArticle(author));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        mockMvc.perform(delete(endPoint)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete someone else's article by admin")
    @WithUserDetails(value = "admin@admin.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteArticle_Someone_Else_By_Admin() throws Exception {
        UserDetails currentAuthenticatedUser = Objects.requireNonNull(AuthService.getCurrentUser());
        User user = userRepository.getByEmail(currentAuthenticatedUser.getUsername());
        Article articleFromDb = articleRepository.save(getArticle(user));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        mockMvc.perform(delete(endPoint)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete article if article is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteArticle_If_Article_Is_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = delete(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete article if is it someone else's")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteArticle_If_Someone_Else() throws Exception {
        User admin = userRepository.getByEmail(getAdmin().getEmail());
        Article articleFromDb = articleRepository.save(getArticle(admin));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleDto contentArticle = new ArticleDto(null, "updated title", "updated description", "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = delete(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();

        String expectedErrorMessage = "It's someone else's article. You can't modify her";
        String actualErrorMessage = resolvedException != null ? resolvedException.getMessage() : "";

        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
    }

    @Test
    @DisplayName("Update article")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateArticle() throws Exception {
        User userFromDb = userRepository.getByEmail(getUser().getEmail());
        Article articleFromDb = articleRepository.save(getArticle(userFromDb));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleDto contentArticle = new ArticleDto(null, "updated title", "updated description", "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleDto articleDto = objectMapper.readValue(response, ArticleDto.class);

        String expectedArticleTitle = "updated title";
        String expectedArticleDescription = "updated description";
        String actualArticleTitle = articleDto.getTitle();
        String actualArticleDescription = articleDto.getDescription();

        Assertions.assertEquals(expectedArticleTitle, actualArticleTitle);
        Assertions.assertEquals(expectedArticleDescription, actualArticleDescription);
    }

    @Test
    @DisplayName("Update article if is it someone else's")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateArticle_If_Someone_Else() throws Exception {
        User admin = userRepository.getByEmail(getAdmin().getEmail());
        Article articleFromDb = articleRepository.save(getArticle(admin));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleDto contentArticle = new ArticleDto(null, "updated title", "updated description", "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        Exception resolvedException = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();

        String expectedErrorMessage = "It's someone else's article. You can't modify her";
        String actualErrorMessage = resolvedException != null ? resolvedException.getMessage() : "";

        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
    }

    @Test
    @DisplayName("Update article if article is not valid")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateArticle_If_Article_Is_Not_Valid() throws Exception {
        User userFromDb = userRepository.getByEmail(getUser().getEmail());
        Article articleFromDb = articleRepository.save(getArticle(userFromDb));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleDto contentArticle = new ArticleDto(null, "test1", "test1", "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //remove first 14 characters to get a list from string
        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedStringViolations = new ArrayList<>(List.of(
                "title should be between 10 and 120 characters",
                "description should be between 10 and 10000 characters"
        ));
        expectedStringViolations.sort(Comparator.naturalOrder());

        List<String> actualStringViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted()
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedStringViolations, actualStringViolations);
    }

    @Test
    @DisplayName("Update article if article is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateArticle_If_Article_Is_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        ArticleDto contentArticle = new ArticleDto(null, "updated title", "updated description", "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    private Article getArticle(User userFromDb) {
        return new Article(null,
                "test article",
                "test",
                "",
                userFromDb);
    }

    private User getUser() {
        return new User(1L,
                "user",
                "user",
                "user@user.com",
                "test",
                "test",
                Set.of(Role.USER),
                List.of());
    }

    private User getAdmin() {
        return new User(2L,
                "admin",
                "admin",
                "admin@admin.com",
                "test",
                "test",
                Set.of(Role.USER, Role.ADMIN),
                List.of());
    }
}
