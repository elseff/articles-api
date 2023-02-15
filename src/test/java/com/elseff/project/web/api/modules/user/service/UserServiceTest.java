package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.security.UserDetailsImpl;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.dto.UserUpdateRequest;
import com.elseff.project.web.api.modules.user.dto.mapper.UserDtoMapper;
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

import java.sql.Timestamp;
import java.time.Instant;
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

    @Mock
    UserDtoMapper userDtoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get all users")
    public void getAllUsers() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(true);
        given(userDtoMapper.mapListUserEntityToDtoForAdmin(any())).willReturn(Arrays.asList(
                new UserDto(),
                new UserDto(),
                new UserDto()
        ));
        given(userRepository.findAll()).willReturn(Arrays.asList(
                new User(),
                new User(),
                new User()
        ));

        List<UserDto> allUsers = service.getAllUsers();

        int expectedListSize = 3;
        int actualListSize = allUsers.size();

        Assertions.assertEquals(expectedListSize, actualListSize);

        verify(userRepository, times(1)).findAll();
        verify(userDtoMapper, times(1)).mapListUserEntityToDtoForAdmin(any());
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(securityUtils);
        verifyNoMoreInteractions(userDtoMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Get user by id")
    void getUserById() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(true);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(new User()));
        given(userDtoMapper.mapUserEntityToDtoForAdmin(any(User.class))).willReturn(new UserDto());

        UserDto user = service.getUserById(1L);

        Assertions.assertNotNull(user);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userDtoMapper, times(1)).mapUserEntityToDtoForAdmin(any(User.class));
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(securityUtils);
        verifyNoMoreInteractions(userDtoMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Get user by id if user is not found")
    void getUserById_If_User_Does_Not_Exists() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        User userFromDb = new User();

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

        User userEntity = getUserEntity();
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();
        userUpdateRequest.setFirstName("test1");
        UserDto userDto = new UserDto();
        userDto.setFirstName("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(userEntity));
        given(securityUtils.userIsAdmin(any(UserDetails.class))).willReturn(true);
        given(userDtoMapper.mapUserEntityToDtoForAdmin(any(User.class))).willReturn(userDto);

        UserDto updatedUser = service.updateUser(1L, userUpdateRequest);

        String expectedFirstName = "test1";
        String actualFirstName = updatedUser.getFirstName();

        Assertions.assertEquals(expectedFirstName, actualFirstName);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(userEntity);
        verify(securityUtils, times(1)).userIsAdmin(any(UserDetails.class));
        verify(userDtoMapper, times(1)).mapUserEntityToDtoForAdmin(any(User.class));
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(securityUtils);
        verifyNoMoreInteractions(userDtoMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update user if profile is someone else's")
    void updateUser_If_Someone_Else_Profile() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("testt");

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
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setFirstName("test1");

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        UserNotFoundException articleNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class, () -> service.updateUser(1L, updateRequest));

        String expectedMessage = "could not find user 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @NotNull
    private UserDetailsImpl getUserDetails() {
        return new UserDetailsImpl(
                "test@test.com",
                "test",
                Set.of(getRoleUser(), getRoleAdmin())
        );
    }

    @NotNull
    private User getUserEntity() {
        User value = new User(
                1L,
                "test",
                "test",
                "test@test.com",
                "test",
                "test",
                Timestamp.from(Instant.now()),
                Set.of(getRoleUser(), getRoleAdmin()),
                List.of()
        );
        return value;
    }

    @NotNull
    private User getDifferentUserEntity() {
        User value = new User(
                2L,
                "testt",
                "testt",
                "test1@test.com",
                "testt",
                "testt",
                Timestamp.from(Instant.now()),
                Set.of(getRoleUser()),
                List.of()
        );
        return value;
    }

    private Role getRoleAdmin() {
        return new Role(2L, "ROLE_ADMIN");
    }

    private Role getRoleUser() {
        return new Role(1L, "ROLE_USER");
    }
}
