package com.elseff.project.web.api.modules.user.controller;

import com.elseff.project.exception.handling.dto.Violation;
import com.elseff.project.persistense.RoleEntity;
import com.elseff.project.persistense.UserEntity;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.article.dto.mapper.ArticleDtoMapper;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.dto.UserUpdateRequest;
import com.elseff.project.web.api.modules.user.dto.mapper.UserDtoMapper;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserControllerTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    UserDtoMapper userDtoMapper;

    @Autowired
    ArticleDtoMapper articleDtoMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    final String endPoint = "/api/v1/users";

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
    }

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        Assertions.assertNotNull(roleRepository);
        Assertions.assertNotNull(userRepository);
        Assertions.assertNotNull(objectMapper);
        Assertions.assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Get all users")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getAllUsers() throws Exception {
        MockHttpServletRequestBuilder request = get(endPoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<UserEntity> users = objectMapper.readValue(response, new TypeReference<>() {
        });

        int expectedListSize = 2;
        int actualListSize = users.size();

        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @DisplayName("Get specific user")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getSpecific() throws Exception {
        UserEntity user = getUser();
        UserEntity userFromDb = userRepository.getByEmail(user.getEmail());
        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserDto userDto = objectMapper.readValue(response, UserDto.class);

        String expectedUserFirstName = userFromDb.getFirstName();
        String actualUserFirstName = userDto.getFirstName();

        Assertions.assertEquals(expectedUserFirstName, actualUserFirstName);
    }

    @Test
    @DisplayName("Get specific if user is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getSpecific_If_User_Not_Found() throws Exception {
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete user by admin")
    @WithUserDetails(value = "admin@admin.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteUser_If_Current_User_Is_Admin() throws Exception {
        UserEntity admin = getAdmin();
        UserEntity userFromDb = userRepository.getByEmail(admin.getEmail());
        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = delete(endPoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        List<UserDto> users = objectMapper.readValue(
                mockMvc.perform(get(this.endPoint))
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        //because there are 2 users in the database, after we delete, there should be one left
        int expectedListSize = 1;
        int actualListSize = users.size();

        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @DisplayName("Delete user by user")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteUser_By_User() throws Exception {
        UserEntity user = getUser();
        UserEntity userFromDb = userRepository.getByEmail(user.getEmail());
        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = delete(endPoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        List<UserDto> users = objectMapper.readValue(
                mockMvc.perform(get(this.endPoint))
                        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        //because there are 2 users in the database, after we delete, there should be one left
        int expectedListSize = 1;
        int actualListSize = users.size();

        Assertions.assertEquals(expectedListSize, actualListSize);
    }

    @Test
    @DisplayName("Delete user if someone else's profile")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void deleteUser_If_Someone_Else_Profile() throws Exception {
        UserEntity admin = getAdmin();
        UserEntity userFromDb = userRepository.getByEmail(admin.getEmail());
        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = delete(endPoint)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);

        Exception exception = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();

        String expectedMessage = "It's someone else's profile. You can't modify him";
        String actualMessage = exception != null ? exception.getMessage() : "";

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Delete user if user is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateUser() throws Exception {
        UserEntity user = getUser();
        UserEntity userFromDb = userRepository.getByEmail(user.getEmail());
        UserUpdateRequest updateRequest = getUserUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = patch(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto userDto = objectMapper.readValue(response, UserDto.class);

        String expectedUpdatedFirstName = updateRequest.getFirstName();
        String actualUpdatedFirstName = userDto.getFirstName();

        Assertions.assertEquals(expectedUpdatedFirstName, actualUpdatedFirstName);
    }

    @Test
    @DisplayName("Update user if user is not found")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateUser_If_User_Not_Found() throws Exception {
        UserUpdateRequest updateRequest = getUserUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(updateRequest);
        String endPoint = this.endPoint + "/" + 0;

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update user if user is not valid")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateUser_If_User_Is_Not_Valid() throws Exception {
        UserEntity user = getUser();
        UserEntity userFromDb = userRepository.getByEmail(user.getEmail());
        UserUpdateRequest updateRequest = getNotValidUserUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(updateRequest);

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

    @Test
    @DisplayName("Update user if it's someone else's profile")
    @WithUserDetails(value = "admin@admin.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void updateUser_If_Someone_Else_Profile() throws Exception {
        UserEntity user = getUser();
        UserEntity userFromDb = userRepository.getByEmail(user.getEmail());
        UserUpdateRequest updateRequest = getUserUpdateRequest();
        String requestBody = objectMapper.writeValueAsString(updateRequest);

        String endPoint = this.endPoint + "/" + userFromDb.getId();

        MockHttpServletRequestBuilder request = patch(endPoint)
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        Exception exception = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResolvedException();

        String expectedMessage = "It's someone else's profile. You can't modify him";
        String actualMessage = exception != null ? exception.getMessage() : "";

        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    @DisplayName("Get me")
    @WithUserDetails(value = "user@user.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getMe() throws Exception {
        String endPoint = this.endPoint + "/me";

        MockHttpServletRequestBuilder request = get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8);

        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        UserDto me = objectMapper.readValue(response, UserDto.class);

        Assertions.assertNotNull(me);

        String expectedMeEmail = "user@user.com";
        String actualMeEmail = me.getEmail();

        Assertions.assertEquals(expectedMeEmail, actualMeEmail);
    }

    private UserEntity getUser() {
        RoleEntity roleUser = roleRepository.getByName("ROLE_USER");
        return UserEntity.builder()
                .id(1L)
                .firstName("user")
                .lastName("user")
                .email("user@user.com")
                .country("test")
                .password("test")
                .roles(Set.of(roleUser))
                .build();
    }

    private UserEntity getAdmin() {
        RoleEntity roleUser = roleRepository.getByName("ROLE_USER");
        RoleEntity roleAdmin = roleRepository.getByName("ROLE_ADMIN");
        return UserEntity.builder()
                .id(2L)
                .firstName("admin")
                .lastName("admin")
                .email("admin@admin.com")
                .country("test")
                .password("test")
                .roles(Set.of(roleUser, roleAdmin))
                .build();
    }

    private UserUpdateRequest getUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .firstName("NewTest")
                .email("test1@test.com")
                .build();
    }

    private UserUpdateRequest getNotValidUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .firstName("test1")
                .email("test")
                .build();
    }
}