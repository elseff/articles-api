package com.elseff.project.web.api.modules.auth.service;

import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.auth.dto.AuthLoginRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthRegisterRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthResponse;
import com.elseff.project.web.api.modules.auth.exception.AuthUserNotFoundException;
import com.elseff.project.web.api.modules.auth.exception.AuthenticationException;
import com.elseff.project.web.api.modules.user.dto.UserAllFieldsDto;
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
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_If_User_Already_Exists() {
        String email = getAuthRegisterRequest().getEmail();

        given(userRepository.existsByEmail(email)).willReturn(true);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class, () -> service.register(getAuthRegisterRequest()));

        String expectedMessage = String.format("User with email %s already exists", email);
        String actualMessage = authenticationException.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void register() {
        String email = getAuthRegisterRequest().getEmail();
        User user = new User();
        user.setEmail(email);
        user.setPassword("test");
        AuthRegisterRequest authRegisterRequest = getAuthRegisterRequest();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(roleRepository.getByName("ROLE_USER")).willReturn(getRoleUser());
        given(modelMapper.map(authRegisterRequest, User.class)).willReturn(user);
        given(passwordEncoder.encode(authRegisterRequest.getPassword())).willReturn("test");
        given(userRepository.save(user)).willReturn(user);

        AuthResponse authResponse = service.register(authRegisterRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = authResponse.getEmail();

        Assertions.assertNotNull(authResponse);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(user);
        verify(modelMapper, times(1)).map(authRegisterRequest, User.class);
        verify(passwordEncoder, times(1)).encode(authRegisterRequest.getPassword());
        verify(roleRepository, times(1)).getByName(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(modelMapper);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(roleRepository);
    }

    @Test
    void login_If_User_Not_Found() {
        String email = "test@test.com";
        AuthLoginRequest authLoginRequest = getAuthLoginRequest();

        given(userRepository.existsByEmail(email)).willReturn(false);

        AuthUserNotFoundException authenticationException = Assertions.assertThrows(AuthUserNotFoundException.class, () -> service.login(authLoginRequest));

        String expectedMessage = String.format("User with email %s is not found", email);
        String actualMessage = authenticationException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).existsByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void login_If_Password_Is_Incorrect() {
        AuthLoginRequest authLoginRequest = getAuthLoginRequest();
        User userFromDb = getUserFromDb();
        String email = userFromDb.getEmail();

        given(userRepository.existsByEmail(email)).willReturn(true);
        given(userRepository.getByEmail(email)).willReturn(userFromDb);
        given(passwordEncoder.matches(authLoginRequest.getPassword(), userFromDb.getPassword())).willReturn(false);

        AuthenticationException authenticationException = Assertions.assertThrows(AuthenticationException.class, () -> service.login(authLoginRequest));

        String expectedMessage = "Incorrect password";
        String actualMessage = authenticationException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).getByEmail(email);
        verify(passwordEncoder, times(1)).matches(authLoginRequest.getPassword(), userFromDb.getPassword());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void login() {
        AuthLoginRequest authLoginRequest = getAuthLoginRequest();
        User userFromDb = getUserFromDb();
        String email = userFromDb.getEmail();

        given(userRepository.existsByEmail(email)).willReturn(true);
        given(userRepository.getByEmail(email)).willReturn(userFromDb);
        given(passwordEncoder.matches(authLoginRequest.getPassword(), userFromDb.getPassword())).willReturn(true);

        AuthResponse login = service.login(authLoginRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = login.getEmail();

        Assertions.assertNotNull(login);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).getByEmail(email);
        verify(passwordEncoder, times(1)).matches(authLoginRequest.getPassword(), userFromDb.getPassword());
        verifyNoMoreInteractions(userRepository);
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

    private AuthLoginRequest getAuthLoginRequest() {
        AuthLoginRequest authLoginRequest = new AuthLoginRequest();
        authLoginRequest.setEmail("test@test.com");
        authLoginRequest.setPassword("test");
        return authLoginRequest;
    }

    private Role getRoleUser() {
        return new Role("ROLE_USER");
    }

    private AuthRegisterRequest getAuthRegisterRequest(){
        AuthRegisterRequest authRegisterRequest = new AuthRegisterRequest();
        authRegisterRequest.setFirstName("Test");
        authRegisterRequest.setLastName("Test");
        authRegisterRequest.setCountry("Test");
        authRegisterRequest.setEmail("test@test.com");
        authRegisterRequest.setPassword("test");
        return authRegisterRequest;
    }
}