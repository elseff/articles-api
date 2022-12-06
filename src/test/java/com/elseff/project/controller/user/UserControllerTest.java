package com.elseff.project.controller.user;

import com.elseff.project.dto.user.UserAllFieldsCanBeNullDto;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.dto.user.UserDto;
import com.elseff.project.dto.validation.Violation;
import com.elseff.project.entity.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
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
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String endPoint = "/api/v1/users";

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
    @DisplayName("Get all users")
    @WithMockUser(username = "test")
    void getAllUsers() throws Exception {
        IntStream.range(0, 5).forEach(value -> repository.save(getUserEntity()));

        MockHttpServletRequestBuilder request = get(endPoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<User> users = objectMapper.readValue(response, new TypeReference<>() {
        });

        int expectedListSize = 5;
        int actualListSize = users.size();

        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Get specific user")
    void getSpecific() throws Exception {
        User userEntity = getUserEntity();
        User userFromDb = repository.save(userEntity);

        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserAllFieldsDto userAllFieldsDto = objectMapper.readValue(response, UserAllFieldsDto.class);

        String expectedUserEmail = userEntity.getEmail();
        String actualUserEmail = userAllFieldsDto.getEmail();

        Assertions.assertEquals(expectedUserEmail, actualUserEmail);
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Get specific if user is not found")
    void getSpecific_If_User_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete user")
    @WithMockUser(username = "test")
    void deleteUser() throws Exception {
        User userFromDb = repository.save(getUserEntity());
        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = delete(endPoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk());

        List<UserDto> users = objectMapper.readValue(
                mockMvc.perform(get(this.endPoint))
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });

        int expectedListSize = 0;
        int actualListSize = users.size();

        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Delete user if user is not found")
    void deleteUser_If_User_Is_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = delete(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update user")
    @WithMockUser(username = "test")
    void updateUser() throws Exception {
        User userFromDb = repository.save(getUserEntity());
        UserAllFieldsCanBeNullDto updateUser = getUserAllFieldsCanBeNullDto();
        String requestBody = objectMapper.writeValueAsString(updateUser);

        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = patch(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserAllFieldsDto userAllFieldsDto = objectMapper.readValue(response, UserAllFieldsDto.class);

        String expectedUpdatedFirstName = updateUser.getFirstName();
        String expectedUpdatedEmail = updateUser.getEmail();
        String actualUpdatedFirstName = userAllFieldsDto.getFirstName();
        String actualUpdatedEmail = userAllFieldsDto.getEmail();

        Assertions.assertEquals(expectedUpdatedEmail, actualUpdatedEmail);
        Assertions.assertEquals(expectedUpdatedFirstName, actualUpdatedFirstName);
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Update user if user is not found")
    void updateUser_If_User_Not_Found() throws Exception {
        UserAllFieldsCanBeNullDto updateUser = getUserAllFieldsCanBeNullDto();
        String requestBody = objectMapper.writeValueAsString(updateUser);
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);


        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test")
    @DisplayName("Update user if user is not valid")
    void updateUser_If_User_Is_Not_Valid() throws Exception {
        User userFromDb = repository.save(getUserEntity());
        UserAllFieldsCanBeNullDto updateUser = getNotValidUserAllFieldsCanBeNullBto();
        String requestBody = objectMapper.writeValueAsString(updateUser);

        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        //remove first 14 characters to get list from string
        String stringViolations = response.substring(14);
        List<Violation> violations = objectMapper.readValue(stringViolations, new TypeReference<>() {
        });

        List<String> expectedStringViolations = new ArrayList<>(List.of(
                "firstname should be valid",
                "email should be valid"
        ));
        expectedStringViolations.sort(Comparator.naturalOrder());

        List<String> actualStringViolations = violations.stream()
                .map(Violation::getMessage)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedStringViolations, actualStringViolations);
    }

    private User getUserEntity() {
        return new User(
                null,
                "test",
                "test",
                "test@test.com",
                "test",
                passwordEncoder.encode("test"),
                Set.of(),
                List.of()
        );
    }

    private UserAllFieldsCanBeNullDto getUserAllFieldsCanBeNullDto() {
        UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto = new UserAllFieldsCanBeNullDto();
        userAllFieldsCanBeNullDto.setFirstName("NewTest");
        userAllFieldsCanBeNullDto.setEmail("test1@test.com");
        return userAllFieldsCanBeNullDto;
    }

    private UserAllFieldsCanBeNullDto getNotValidUserAllFieldsCanBeNullBto() {
        UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto = new UserAllFieldsCanBeNullDto();
        userAllFieldsCanBeNullDto.setFirstName("test1");
        userAllFieldsCanBeNullDto.setEmail("test");
        return userAllFieldsCanBeNullDto;
    }
}