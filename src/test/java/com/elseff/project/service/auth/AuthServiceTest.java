package com.elseff.project.service.auth;

import com.elseff.project.dto.auth.AuthRequest;
import com.elseff.project.dto.auth.AuthResponse;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.entity.User;
import com.elseff.project.exception.auth.AuthenticationException;
import com.elseff.project.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService service;

    @Mock
    private UserRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_If_User_Already_Exists() {
        String email = getUserAllFieldsDto().getEmail();

        given(repository.existsByEmail(email)).willReturn(true);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class, () -> service.register(getUserAllFieldsDto()));

        String expectedMessage = String.format("User with email %s already exists", email);
        String actualMessage = authenticationException.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));

        verify(repository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void register() {
        String email = getUserAllFieldsDto().getEmail();
        User user = new User();
        user.setEmail(email);
        user.setPassword("test");
        UserAllFieldsDto userAllFieldsDto = getUserAllFieldsDto();

        given(repository.existsByEmail(email)).willReturn(false);
        given(modelMapper.map(userAllFieldsDto, User.class)).willReturn(user);
        given(passwordEncoder.encode(userAllFieldsDto.getPassword())).willReturn("test");
        given(repository.save(user)).willReturn(user);

        AuthResponse authResponse = service.register(userAllFieldsDto);

        String expectedEmail = "test@test.com";
        String actualEmail = authResponse.getEmail();

        Assertions.assertNotNull(authResponse);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(repository, times(1)).existsByEmail(anyString());
        verify(modelMapper, times(1)).map(userAllFieldsDto, User.class);
        verify(passwordEncoder, times(1)).encode(userAllFieldsDto.getPassword());
        verify(repository, times(1)).save(user);
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(modelMapper);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void login_If_User_Not_Found() {
        String email = "test@test.com";
        AuthRequest authRequest = getAuthRequest();

        given(repository.existsByEmail(email)).willReturn(false);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class, () -> service.login(authRequest));

        String expectedMessage = String.format("User with email %s is not found", email);
        String actualMessage = authenticationException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void login_If_Password_Is_Incorrect() {
        AuthRequest authRequest = getAuthRequest();
        User userFromDb = getUserFromDb();
        String email = userFromDb.getEmail();

        given(repository.existsByEmail(email)).willReturn(true);
        given(repository.getByEmail(email)).willReturn(userFromDb);
        given(passwordEncoder.matches(authRequest.getPassword(), userFromDb.getPassword())).willReturn(false);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class, () -> service.login(authRequest));

        String expectedMessage = "Incorrect password";
        String actualMessage = authenticationException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(repository, times(1)).existsByEmail(email);
        verify(repository, times(1)).getByEmail(email);
        verify(passwordEncoder, times(1)).matches(authRequest.getPassword(), userFromDb.getPassword());
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void login() {
        AuthRequest authRequest = getAuthRequest();
        User userFromDb = getUserFromDb();
        String email = userFromDb.getEmail();

        given(repository.existsByEmail(email)).willReturn(true);
        given(repository.getByEmail(email)).willReturn(userFromDb);
        given(passwordEncoder.matches(authRequest.getPassword(), userFromDb.getPassword())).willReturn(true);

        AuthResponse login = service.login(authRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = login.getEmail();

        Assertions.assertNotNull(login);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(repository, times(1)).existsByEmail(email);
        verify(repository, times(1)).getByEmail(email);
        verify(passwordEncoder, times(1)).matches(authRequest.getPassword(), userFromDb.getPassword());
        verifyNoMoreInteractions(repository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    private User getUserFromDb() {
        User user = new User();
        user.setPassword("test");
        user.setEmail("test@test.com");
        user.setCountry("test");
        user.setFirstName("test");
        user.setLastName("test");
        return user;
    }

    private AuthRequest getAuthRequest() {
        return new AuthRequest("test@test.com", "test");
    }

    private UserAllFieldsDto getUserAllFieldsDto() {
        UserAllFieldsDto userAllFieldsDto = new UserAllFieldsDto();
        userAllFieldsDto.setEmail("test@test.com");
        userAllFieldsDto.setPassword("test");
        return userAllFieldsDto;
    }
}