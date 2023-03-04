package com.elseff.project.web.api.modules.article.controller;

import com.elseff.project.exception.handling.dto.Violation;
import com.elseff.project.persistense.ArticleEntity;
import com.elseff.project.persistense.RoleEntity;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.ArticleRepository;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.article.dto.ArticleCreationRequest;
import com.elseff.project.web.api.modules.article.dto.ArticleDto;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ArticleControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    final String endPoint = "/api/v1/articles";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        //first of all clear the users
        userRepository.deleteAll();
        //and just after that clear the roles
        roleRepository.deleteAll();

        //saving roles user and admin
        roleRepository.save(new RoleEntity("ROLE_USER"));
        roleRepository.save(new RoleEntity("ROLE_ADMIN"));
        //send changes to db
        roleRepository.flush();

        //saving user and admin
        userRepository.save(getUser());
        userRepository.save(getAdmin());

        //clear the articles
        articleRepository.deleteAll();
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        Assertions.assertNotNull(articleRepository);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(roleRepository);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Find all Articles")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAll() throws Exception {
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
    @DisplayName("Find all articles by author id")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findAllByAuthorId() throws Exception {
        User user = userRepository.getByEmail(getUser().getEmail());
        User admin = userRepository.getByEmail(getAdmin().getEmail());

        //saving 2 article with author user and 1 article with author admin
        articleRepository.save(getArticle(user));
        articleRepository.save(getArticle(user));
        articleRepository.save(getArticle(admin));

        MockHttpServletRequestBuilder request = get(endPoint)
                .param("authorId", user.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ArticleEntity> articles = objectMapper.readValue(response, new TypeReference<>() {
        });

        int expectedListSize = 2;
        int actualListSize = articles.size();

        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @DisplayName("Find article")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void findById() throws Exception {
        User currentAuthenticatedUser = userRepository.getByEmail(getUser().getEmail());
        ArticleEntity articleFromDb = articleRepository.save(getArticle(currentAuthenticatedUser));
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
    @DisplayName("Find article if article is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void FindArticleById_If_Article_Is_Not_Found() throws Exception {
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
        ArticleCreationRequest articleCreationRequest = ArticleCreationRequest.builder()
                .title("test add new article title")
                .description("test add new article description")
                .build();

        String requestBody = objectMapper.writeValueAsString(articleCreationRequest);

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ArticleDto responseArticle = objectMapper.readValue(response, ArticleDto.class);

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
        ArticleCreationRequest articleCreationRequest = ArticleCreationRequest.builder()
                .title("test")
                .description("test")
                .build();

        String requestBody = objectMapper.writeValueAsString(articleCreationRequest);

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
        UserDetails currentAuthenticatedUser = Objects.requireNonNull(AuthService.getCurrentUser());
        User author = userRepository.getByEmail(currentAuthenticatedUser.getUsername());
        ArticleEntity articleFromDb = articleRepository.save(getArticle(author));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        mockMvc.perform(delete(endPoint)).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete someone else's article by admin")
    @WithUserDetails(value = "admin@admin.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteArticle_Someone_Else_By_Admin() throws Exception {
        UserDetails currentAuthenticatedUser = Objects.requireNonNull(AuthService.getCurrentUser());
        User user = userRepository.getByEmail(currentAuthenticatedUser.getUsername());
        ArticleEntity articleFromDb = articleRepository.save(getArticle(user));
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
        ArticleEntity articleFromDb = articleRepository.save(getArticle(admin));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleDto contentArticle = ArticleDto.builder()
                .title("updated title")
                .description("updatedDescription")
                .build();

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
        ArticleEntity articleFromDb = articleRepository.save(getArticle(userFromDb));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleCreationRequest articleCreationRequest = ArticleCreationRequest.builder()
                .title("updated title")
                .description("updated description")
                .build();

        String requestBody = objectMapper.writeValueAsString(articleCreationRequest);

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
        ArticleEntity articleFromDb = articleRepository.save(getArticle(admin));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleCreationRequest articleCreationRequest = ArticleCreationRequest.builder()
                .title("updated title")
                .description("updated description")
                .build();

        String requestBody = objectMapper.writeValueAsString(articleCreationRequest);

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
        ArticleEntity articleFromDb = articleRepository.save(getArticle(userFromDb));
        String endPoint = this.endPoint + "/" + articleFromDb.getId();

        ArticleCreationRequest articleCreationRequest = ArticleCreationRequest.builder()
                .title("test1")
                .description("test1")
                .build();

        String requestBody = objectMapper.writeValueAsString(articleCreationRequest);

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

        ArticleCreationRequest articleCreationRequest = ArticleCreationRequest.builder()
                .title("updated title")
                .description("updated description")
                .build();

        String requestBody = objectMapper.writeValueAsString(articleCreationRequest);

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    private ArticleEntity getArticle(User userFromDb) {
        return ArticleEntity.builder()
                .title("test article")
                .description("test")
                .author(userFromDb)
                .build();
    }

    private User getUser() {
        RoleEntity roleUser = roleRepository.getByName("ROLE_USER");

        return User.builder()
                .firstName("user")
                .lastName("user")
                .email("user@user.com")
                .country("test")
                .password("test")
                .roles(Set.of(roleUser))
                .build();
    }

    private User getAdmin() {
        RoleEntity roleUser = roleRepository.getByName("ROLE_USER");
        RoleEntity roleAdmin = roleRepository.getByName("ROLE_ADMIN");

        return User.builder()
                .firstName("admin")
                .lastName("admin")
                .email("admin@admin.com")
                .country("test")
                .password("test")
                .roles(Set.of(roleUser, roleAdmin))
                .build();
    }
}
