package com.elseff.project.web.api.modules.auth.controller;

import com.elseff.project.exception.handling.dto.Violation;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.auth.dto.AuthLoginRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthRegisterRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private final String endPoint = "/api/v1/auth";

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        //clear all users
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        Assertions.assertNotNull(passwordEncoder);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Register")
    void register() throws Exception {
        AuthRegisterRequest registerRequest = getAuthRegisterRequest();

        String requestBody = objectMapper.writeValueAsString(registerRequest);

        String endPoint = this.endPoint + "/register";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Register if email is already registered")
    void register_If_Email_Is_Already_Registered() throws Exception {
        userRepository.save(getUserEntity());

        AuthRegisterRequest registerRequest = getAuthRegisterRequest();
        String contentUser = objectMapper.writeValueAsString(registerRequest);

        String endPoint = this.endPoint + "/register";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentUser)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register if user is not valid")
    void register_If_User_Is_Not_Valid() throws Exception {
        AuthRegisterRequest registerRequest = getNotValidAuthRegisterRequest();
        String contentUser = objectMapper.writeValueAsString(registerRequest);

        String endPoint = this.endPoint + "/register";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentUser)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //remove first 14 characters to get a list from string
        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedViolations = new ArrayList<>(List.of(
                "firstname should be valid",
                "lastname should be valid",
                "password size should be greater than 4",
                "country should be valid",
                "email should be valid"
        ));
        expectedViolations.sort(Comparator.naturalOrder());

        List<String> actualViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    @Test
    @DisplayName("Log in")
    void login() throws Exception {
        userRepository.save(getUserEntity());

        AuthLoginRequest authLoginRequest = getValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);

        String expectedAuthToken = Base64.encodeBase64String(
                (authLoginRequest.getEmail() + ":" + authLoginRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
        String actualAuthToken = authResponse.getToken();

        Assertions.assertEquals(expectedAuthToken, actualAuthToken);
    }

    @Test
    @DisplayName("Log in if user is not found")
    void login_If_User_Is_Not_Found() throws Exception {
        AuthLoginRequest authLoginRequest = getValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Log in if password is incorrect")
    void login_If_Password_Is_Incorrect() throws Exception {
        User userEntity = getUserEntity();
        userEntity.setPassword(passwordEncoder.encode("test"));
        userRepository.save(userEntity);

        AuthLoginRequest authLoginRequest = getValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Log in if AuthRequest is not valid")
    void login_If_AuthRequest_Is_Not_Valid() throws Exception {
        User userEntity = userRepository.save(getUserEntity());

        AuthLoginRequest authLoginRequest = getNotValidAuthRequest();
        String contentAuthRequest = objectMapper.writeValueAsString(authLoginRequest);

        String endPoint = this.endPoint + "/login";

        MockHttpServletRequestBuilder request = post(endPoint)
                .content(contentAuthRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //remove first 14 characters to get a list from string
        String stringList = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringList, new TypeReference<>() {
        });

        List<String> expectedViolations = new ArrayList<>(List.of(
                "email should be valid",
                "password size should be greater than 4"
        ));
        expectedViolations.sort(Comparator.naturalOrder());

        List<String> actualViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedViolations, actualViolations);
    }

    private AuthRegisterRequest getAuthRegisterRequest() {
        return new AuthRegisterRequest(
                "TestFirstName",
                "TestLastName",
                "test@test.com",
                "Test",
                "test"
        );
    }

    private User getUserEntity() {
        return new User(
                null,
                "test",
                "test",
                "test@test.com",
                "test",
                passwordEncoder.encode("root"),
                Timestamp.from(Instant.now()),
                Set.of(),
                List.of());
    }

    private AuthRegisterRequest getNotValidAuthRegisterRequest() {
        return new AuthRegisterRequest(
                "test",
                "test",
                "test",
                "test",
                "t"
        );
    }

    private AuthLoginRequest getValidAuthRequest() {
        return new AuthLoginRequest(
                "test@test.com",
                "root");
    }

    private AuthLoginRequest getNotValidAuthRequest() {
        return new AuthLoginRequest(
                "t",
                "t");
    }
}