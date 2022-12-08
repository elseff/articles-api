package com.elseff.project.controller.article;

import com.elseff.project.dto.article.ArticleAllFieldsDto;
import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.dto.validation.Violation;
import com.elseff.project.entity.Article;
import com.elseff.project.entity.User;
import com.elseff.project.repository.ArticleRepository;
import com.elseff.project.repository.UserRepository;
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
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc(printOnlyOnFailure = false)
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
    @DisplayName("All Articles")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getArticles() throws Exception {
        User currentAuthenticatedUser = userRepository.findByEmail(getUser().getEmail());
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
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getArticleById() throws Exception {
        User currentAuthenticatedUser = userRepository.findByEmail(getUser().getEmail());
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
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ArticleAllFieldsDto responseArticle = objectMapper.readValue(response, ArticleAllFieldsDto.class);

        String expectedArticleTitle = "test add new article title";
        String actualArticleTitle = responseArticle.getTitle();

        Assertions.assertNotNull(responseArticle);
        Assertions.assertEquals(expectedArticleTitle, actualArticleTitle);
    }

    @Test
    @DisplayName("Add article if article is not valid")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @DisplayName("Delete article")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteArticle() throws Exception {
        User currentAuthenticatedUser = userRepository.findByEmail(getUser().getEmail());
        Article articleFromDb = articleRepository.save(getArticle(currentAuthenticatedUser));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        mockMvc.perform(delete(endPoint)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Delete article if article is not found")
    void deleteArticle_If_Article_Is_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = delete(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update article")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateArticle() throws Exception {
        User userFromDb = userRepository.findByEmail(getUser().getEmail());
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
    @DisplayName("Update article if article is not valid")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateArticle_If_Article_Is_Not_Valid() throws Exception {
        User userFromDb = userRepository.findByEmail(getUser().getEmail());
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
    @WithMockUser(username = "test")
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
        return new Article(null, "test article", "test", "", userFromDb);
    }

    private User getUser() {
        return new User(1L,
                "test",
                "test",
                "test@test.com",
                "test",
                "test",
                Set.of(),
                List.of());
    }
}
