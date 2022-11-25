package com.elseff.project.controller.article;

import com.elseff.project.dto.article.ArticleDto;
import com.elseff.project.dto.validation.Violation;
import com.elseff.project.entity.Article;
import com.elseff.project.repository.ArticleRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
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
    private MockMvc mockMvc;

    @Autowired
    private ArticleRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String endPoint = "/api/v1/articles";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(mockMvc);
        Assertions.assertNotNull(repository);
    }

    @Test
    @DisplayName("All Articles")
    @WithMockUser(username = "test")
    void getArticles() throws Exception {
        repository.save(new Article(null, "test article 1", "test", ""));
        repository.save(new Article(null, "test article 2", "test", ""));
        repository.save(new Article(null, "test article 3", "test", ""));

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<ArticleDto> listArticles = this.objectMapper.readValue(response, new TypeReference<>() {
        });
        listArticles.forEach(a -> System.out.println(a.toString()));
        Assertions.assertNotNull(listArticles);

        if (listArticles.size() != 0)
            Assertions.assertNotNull(listArticles.get(0));
    }

    @Test
    @DisplayName("Get article")
    @WithMockUser(username = "test")
    void getArticleById() throws Exception {
        Long savedArticleId = repository.save(new Article(null, "test article", "test", "")).getId();
        String endPoint = this.endPoint + "/" + savedArticleId;

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
    @WithMockUser(username = "test")
    @DisplayName("Get article if article is not found")
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
    @WithMockUser(username = "test")
    void addArticle() throws Exception {
        ArticleDto contentArticle = new ArticleDto(
                null,
                "test add new article title",
                "test add new article description",
                "");

        String requestBody = objectMapper.writeValueAsString(contentArticle);

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ArticleDto responseArticle = objectMapper.readValue(response, ArticleDto.class);

        String expectedArticleTitle = "test add new article title";
        String actualArticleTitle = responseArticle.getTitle();

        Assertions.assertNotNull(responseArticle);
        Assertions.assertEquals(expectedArticleTitle, actualArticleTitle);
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Add article if article is not valid")
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

        Set<String> expectedStringViolations = new HashSet<>();
        expectedStringViolations.add("title should be between 10 and 120 characters");
        expectedStringViolations.add("description should be between 10 and 10000 characters");

        Set<String> actualStringViolations = violations.stream().map(Violation::getMessage).collect(Collectors.toSet());

        Assertions.assertEquals(expectedStringViolations, actualStringViolations);
    }

    @Test
    @DisplayName("Delete article")
    @WithMockUser(username = "test")
    void deleteArticle() throws Exception {
        Long savedArticleId = repository.save(new Article(null, "test", "test", "")).getId();
        String endPoint = this.endPoint + "/" + savedArticleId;

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
    @WithMockUser(username = "test")
    void updateArticle() throws Exception {
        Long savedArticleId = repository.save(new Article(null, "test", "test", "")).getId();
        String endPoint = this.endPoint + "/" + savedArticleId;

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
    @WithMockUser(username = "test")
    @DisplayName("Update article if article is not valid")
    void updateArticle_If_Article_Is_Not_Valid() throws Exception {
        Long savedArticleId = repository.save(new Article(null, "test", "test", "")).getId();
        String endPoint = this.endPoint + "/" + savedArticleId;

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

        Set<String> expectedStringViolations = new HashSet<>();
        expectedStringViolations.add("title should be between 10 and 120 characters");
        expectedStringViolations.add("description should be between 10 and 10000 characters");

        Set<String> actualStringViolations = violations.stream().map(Violation::getMessage).collect(Collectors.toSet());

        Assertions.assertEquals(expectedStringViolations, actualStringViolations);
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Update article if article is not found")
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
}
