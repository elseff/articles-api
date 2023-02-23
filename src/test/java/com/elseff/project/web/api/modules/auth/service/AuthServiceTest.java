package com.elseff.project.web.api.modules.auth.service;

import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.JwtProvider;
import com.elseff.project.web.api.modules.auth.dto.AuthLoginRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthRegisterRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthResponse;
import com.elseff.project.web.api.modules.auth.exception.AuthUserNotFoundException;
import com.elseff.project.web.api.modules.auth.exception.AuthenticationException;
import com.elseff.project.web.api.modules.user.dto.mapper.UserDtoMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class AuthServiceTest {

    @InjectMocks
    AuthService service;

    @Mock
    UserRepository userRepository;

    @Mock
    UserDtoMapper userDtoMapper;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtProvider jwtProvider;

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
        User user = User.builder()
                .email(email)
                .password("test")
                .build();
        AuthRegisterRequest authRegisterRequest = getAuthRegisterRequest();

        given(userRepository.existsByEmail(email)).willReturn(false);
        given(roleRepository.getByName("ROLE_USER")).willReturn(getRoleUser());
        given(userDtoMapper.mapAuthRequestToUserEntity(authRegisterRequest)).willReturn(user);
        given(passwordEncoder.encode(authRegisterRequest.getPassword())).willReturn("test");
        given(userRepository.save(user)).willReturn(user);
        given(jwtProvider.generateToken(anyString())).willReturn("token");

        AuthResponse authResponse = service.register(authRegisterRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = authResponse.getEmail();

        Assertions.assertNotNull(authResponse);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(user);
        verify(userDtoMapper, times(1)).mapAuthRequestToUserEntity(authRegisterRequest);
        verify(passwordEncoder, times(1)).encode(authRegisterRequest.getPassword());
        verify(roleRepository, times(1)).getByName(anyString());
        verify(jwtProvider, times(1)).generateToken(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(userDtoMapper);
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoMoreInteractions(roleRepository);
        verifyNoMoreInteractions(jwtProvider);
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
        given(jwtProvider.generateToken(anyString())).willReturn("token");

        AuthResponse login = service.login(authLoginRequest);

        String expectedEmail = "test@test.com";
        String actualEmail = login.getEmail();

        Assertions.assertNotNull(login);
        Assertions.assertEquals(expectedEmail, actualEmail);

        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, times(1)).getByEmail(email);
        verify(passwordEncoder, times(1)).matches(authLoginRequest.getPassword(), userFromDb.getPassword());
        verify(jwtProvider, times(1)).generateToken(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(passwordEncoder);
    }

    private User getUserFromDb() {
        return User.builder()
                .firstName("test")
                .lastName("test")
                .email("test@test.com")
                .country("test")
                .password("test")
                .build();
    }

    private AuthLoginRequest getAuthLoginRequest() {
        return AuthLoginRequest.builder()
                .email("test@test.com")
                .password("test")
                .build();
    }

    private Role getRoleUser() {
        return new Role("ROLE_USER");
    }

    private AuthRegisterRequest getAuthRegisterRequest() {
        return AuthRegisterRequest.builder()
                .firstName("Test")
                .lastName("Test")
                .country("Test")
                .email("test@test.com")
                .password("test")
                .build();
    }
}