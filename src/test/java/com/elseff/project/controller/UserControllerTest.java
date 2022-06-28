package com.elseff.project.controller;


import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.dto.user.UserDto;
import com.elseff.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Slf4j
class UserControllerTest {

    @InjectMocks
    private UserController controller;

    @Mock
    private UserService service;

    private final MockMvc mockMvc;

    private final ObjectMapper mapper;

    private static final String endPoint = "/users";

    public UserControllerTest() {
        MockitoAnnotations.openMocks(this);
        when(service.getAllUsers()).thenReturn(Arrays.asList(
                new UserDto(),
                new UserDto(),
                new UserDto()
        ));
        mockMvc = standaloneSetup(controller).build();
        this.mapper = new ObjectMapper();
    }

    @Test
    void getAllUsers() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(endPoint)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void incorrectPath() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/" + anyString())
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(builder).andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void addUser() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new UserAllFieldsDto(1L, "Test", "Test", "Test@email.test", "Test")));
        mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void addUserIfFieldsNotValid() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new UserAllFieldsDto(1L, "11test", "22test", "testemail", "test")));
        mockMvc.perform(builder).andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void addUserIfIdLessThenZero() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new UserAllFieldsDto(-5L, "Test", "Test", "Test@email.test", "Test")));
        mockMvc.perform(builder).andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void getSpecific() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(endPoint + "/1")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void deleteUser() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .delete(endPoint + "/1")
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(builder).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

}