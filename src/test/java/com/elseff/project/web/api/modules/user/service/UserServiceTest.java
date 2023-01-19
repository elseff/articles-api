package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.UserDetailsImpl;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.elseff.project.web.api.modules.user.dto.UserAllFieldsCanBeNullDto;
import com.elseff.project.web.api.modules.user.dto.UserAllFieldsDto;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.exception.SomeoneElseUserProfileException;
import com.elseff.project.web.api.modules.user.exception.UserNotFoundException;
import lombok.Cleanup;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get all users")
    public void getAllUsers() {
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
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Get user by id")
    void getUserById() {
        User userFromDb = new User();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(userFromDb));
        given(modelMapper.map(userFromDb, UserAllFieldsDto.class)).willReturn(new UserAllFieldsDto());

        UserAllFieldsDto user = service.getUserById(anyLong());

        Assertions.assertNotNull(user);

        verify(userRepository, times(1)).findById(anyLong());
        verify(modelMapper, times(1)).map(userFromDb, UserAllFieldsDto.class);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(modelMapper);
    }

    @Test
    @DisplayName("Get user by id if user is not found")
    void getUserById_When_User_Does_Not_Exists() {
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
    @DisplayName("Delete user by admin")
    void deleteUser_If_Current_User_Is_Admin() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getUserEntity()));
        given(roleRepository.getByName("ROLE_ADMIN")).willReturn(getRoleAdmin());
        given(roleRepository.getByName("ROLE_USER")).willReturn(getRoleUser());
        willDoNothing().given(userRepository).deleteById(anyLong());

        service.deleteUser(0L);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).deleteById(anyLong());
        verify(roleRepository,times(1)).getByName(anyString());
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(roleRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Delete user if user is not found")
    void deleteUser_If_User_Not_Exists() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl user = getUserDetails();

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(user);
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class, () ->
                service.deleteUser(5L));

        String expectedMessage = "could not find user 5";
        String actualMessage = exception.getMessage();

        Assertions.assertTrue(actualMessage.contains(expectedMessage));

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update user")
    void updateUser() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        User userEntity = getUserEntity();
        UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto = new UserAllFieldsCanBeNullDto();
        userAllFieldsCanBeNullDto.setFirstName("test1");
        UserAllFieldsDto userAllFieldsDto = new UserAllFieldsDto();
        userAllFieldsDto.setFirstName("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(userEntity));
        given(modelMapper.map(userEntity, UserAllFieldsDto.class)).willReturn(userAllFieldsDto);

        UserAllFieldsDto updatedUser = service.updateUser(1L, userAllFieldsCanBeNullDto);

        String expectedFirstName = "test1";
        String actualFirstName = updatedUser.getFirstName();

        Assertions.assertEquals(expectedFirstName, actualFirstName);

        verify(userRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).save(userEntity);
        verify(modelMapper, times(1)).map(userEntity, UserAllFieldsDto.class);
        verifyNoMoreInteractions(userRepository);
        verifyNoMoreInteractions(modelMapper);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("Update user if profile is someone else's")
    void updateUser_If_Someone_Else_Profile() {
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto = new UserAllFieldsCanBeNullDto();
        userAllFieldsCanBeNullDto.setFirstName("testt");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(getDifferentUserEntity()));

        SomeoneElseUserProfileException exception = Assertions.assertThrows(SomeoneElseUserProfileException.class,
                () -> service.updateUser(1L, userAllFieldsCanBeNullDto));

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
        @Cleanup
        MockedStatic<AuthService> serviceMockedStatic = Mockito.mockStatic(AuthService.class);
        UserDetailsImpl userDetails = getUserDetails();
        UserAllFieldsCanBeNullDto userDto = new UserAllFieldsCanBeNullDto();
        userDto.setFirstName("test1");

        serviceMockedStatic.when(AuthService::getCurrentUser).thenReturn(userDetails);
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        UserNotFoundException articleNotFoundException =
                Assertions.assertThrows(UserNotFoundException.class, () -> service.updateUser(1L, userDto));

        String expectedMessage = "could not find user 1";
        String actualMessage = articleNotFoundException.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        verify(userRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(userRepository);
        serviceMockedStatic.verify(AuthService::getCurrentUser, times(1));
        serviceMockedStatic.verifyNoMoreInteractions();
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
                Set.of(getRoleUser()),
                List.of()
        );
        return value;
    }

    private Role getRoleAdmin() {
        return new Role(1L, "ROLE_ADMIN");
    }

    private Role getRoleUser() {
        return new Role(1L, "ROLE_USER");
    }
}
