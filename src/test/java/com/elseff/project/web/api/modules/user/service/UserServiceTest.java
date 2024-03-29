package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistense.RoleEntity;
import com.elseff.project.persistense.UserEntity;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.security.UserDetailsImpl;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.dto.UserUpdateRequest;
import com.elseff.project.web.api.modules.user.exception.SomeoneElseUserProfileException;
import com.elseff.project.web.api.modules.user.exception.UserNotFoundException;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
class UserServiceTest {

    @InjectMocks
    UserService service;

    @Mock
    UserRepository userRepository;

    @Mock
    SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get all users")
    public void getAllUsers() {
        given(userRepository.findAll()).willReturn(Arrays.asList(
                new UserEntity(),
                new UserEntity(),
                new UserEntity()
        ));

        List<UserEntity> allUsers = service.getAllUsers();

        int expectedListSize = 3;
        int actualListSize = allUsers.size();

        Assertions.assertEquals(expectedListSize, actualListSize);

        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Get user by id")
    void getUserById() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(new UserEntity()));

        UserEntity user = service.getUserById(1L);

        Assertions.assertNotNull(user);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Get user by id if user is not found")
    void getUserById_If_User_Does_Not_Exists() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        UserEntity userFromDb = new UserEntity();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class, () ->
                service.getUserById(5L));

        String expectedMessage = "could not find user 5";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Delete user by himself")
    void deleteUser() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(false);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getUserEntity()));
        willDoNothing().given(userRepository).deleteById(anyLong());

        service.deleteUser(1L);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(securityUtils);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete user is someone else's profile")
    void deleteUser_If_Someone_Else_Profile() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();


        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getDifferentUserEntity()));
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(false);

        SomeoneElseUserProfileException exception = Assertions.assertThrows(SomeoneElseUserProfileException.class,
                () -> service.deleteUser(1L));

        String expectedMessage = "It's someone else's profile. You can't modify him";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findById(anyLong());
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();

    }

    @Test
    @DisplayName("Delete user by admin")
    void deleteUser_If_Current_User_Is_Admin() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(true);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getUserEntity()));
        willDoNothing().given(userRepository).deleteById(anyLong());

        service.deleteUser(0L);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(securityUtils);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete user if user is not found")
    void deleteUser_If_User_Not_Exists() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class, () ->
                service.deleteUser(5L));

        String expectedMessage = "could not find user 5";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Update user")
    void updateUser() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();

        UserEntity userEntity = getUserEntity();
        UserUpdateRequest userUpdateRequest = UserUpdateRequest.builder()
                .firstName("test1")
                .build();
        UserDto userDto = UserDto.builder()
                .firstName("test1")
                .build();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(userEntity));

        UserEntity updatedUser = service.updateUser(1L, userUpdateRequest);

        String expectedFirstName = "test1";
        String actualFirstName = updatedUser.getFirstName();

        Assertions.assertEquals(expectedFirstName, actualFirstName);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(userEntity);
        verifyNoMoreInteractions(userRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update user if profile is someone else's")
    void updateUser_If_Someone_Else_Profile() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("testt")
                .build();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getDifferentUserEntity()));

        SomeoneElseUserProfileException exception = Assertions.assertThrows(SomeoneElseUserProfileException.class,
                () -> service.updateUser(1L, updateRequest));

        String expectedMessage = "It's someone else's profile. You can't modify him";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update user if user is not found")
    void updateUser_If_User_Is_Not_Found() {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .firstName("test1")
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        UserNotFoundException articleNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class, () -> service.updateUser(1L, updateRequest));

        String expectedMessage = "could not find user 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Get me")
    void getMe() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        UserEntity user = getUserEntity();
        UserDto userDto = UserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.getByEmail(userDetails.getEmail())).willReturn(user);

        UserEntity me = service.getMe();

        Assertions.assertNotNull(userDto);

        String expectedUserEmail = "test@test.com";
        String actualUserEmail = me.getEmail();

        verify(userRepository, times(1)).getByEmail(anyString());
        verifyNoMoreInteractions(userRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @NotNull
    private UserDetailsImpl getUserDetails() {
        return UserDetailsImpl.builder()
                .email("test@test.com")
                .password("test")
                .grantedAuthorities(Set.of(getRoleUser(), getRoleAdmin()))
                .build();
    }

    @NotNull
    private UserEntity getUserEntity() {
        return UserEntity.builder()
                .id(2L)
                .firstName("test")
                .lastName("test")
                .email("test@test.com")
                .country("test")
                .password("test")
                .roles(Set.of(getRoleUser(), getRoleAdmin()))
                .build();
    }

    @NotNull
    private UserEntity getDifferentUserEntity() {
        return UserEntity.builder()
                .id(2L)
                .firstName("testt")
                .lastName("testt")
                .email("test1@test.com")
                .country("testt")
                .password("testt")
                .roles(Set.of(getRoleUser()))
                .build();
    }

    private RoleEntity getRoleAdmin() {
        return new RoleEntity(2L, "ROLE_ADMIN");
    }

    private RoleEntity getRoleUser() {
        return new RoleEntity(1L, "ROLE_USER");
    }
}
