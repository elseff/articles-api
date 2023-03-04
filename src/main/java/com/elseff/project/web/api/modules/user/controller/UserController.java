package com.elseff.project.web.api.modules.user.controller;

import com.elseff.project.persistense.User;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.dto.UserUpdateRequest;
import com.elseff.project.web.api.modules.user.dto.mapper.UserDtoMapper;
import com.elseff.project.web.api.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User controller", description = "User management")
public class UserController {

    UserService userService;

    UserDtoMapper userDtoMapper;

    SecurityUtils securityUtils;

    @Operation(summary = "Get all users",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    ),
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        List<User> users = userService.getAllUsers();

        boolean currentUserIsAdmin = securityUtils.userIsAdmin(Objects.requireNonNull(AuthService.getCurrentUser()));

        return currentUserIsAdmin ? userDtoMapper.mapListUserEntityToDtoForAdmin(users)
                : userDtoMapper.mapListUserEntityToDtoForUser(users);
    }

    @Operation(summary = "Get specific user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            }
    )
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getSpecific(@Parameter(description = "User id")
                               @PathVariable Long id) {
        User user = userService.getUserById(id);

        boolean currentUserIsAdmin = securityUtils.userIsAdmin(Objects.requireNonNull(AuthService.getCurrentUser()));

        return currentUserIsAdmin ? userDtoMapper.mapUserEntityToDtoForAdmin(user)
                : userDtoMapper.mapUserEntityToDtoForUser(user);
    }

    @Operation(summary = "Delete user",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "No content. User has been successfully deleted",
                            content = {@Content()}
                    ),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@Parameter(description = "User id")
                           @PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Update user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User has been successfully updated",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "User not valid", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            }
    )
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@Parameter(description = "Updated user")
                              @RequestBody
                              @Valid
                                      UserUpdateRequest updateRequest,
                              @Parameter(description = "User id")
                              @PathVariable
                                      Long id) {
        User user = userService.updateUser(id, updateRequest);

        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());
        boolean currentUserIsAdmin = securityUtils.userIsAdmin(currentUser);

        return currentUserIsAdmin ? userDtoMapper.mapUserEntityToDtoForAdmin(user)
                : userDtoMapper.mapUserEntityToDtoForUser(user);
    }

    @Operation(summary = "User Profile",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User profile",
                            content = @Content(schema = @Schema(implementation = UserDto.class))
                    )
            }
    )
    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getMe() {
        User me = userService.getMe();

        return userDtoMapper.mapUserEntityToDtoForAdmin(me);
    }
}
