package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.dto.UserUpdateRequest;
import com.elseff.project.web.api.modules.user.dto.mapper.UserDtoMapper;
import com.elseff.project.web.api.modules.user.exception.SomeoneElseUserProfileException;
import com.elseff.project.web.api.modules.user.exception.UserNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;

    UserDtoMapper userDtoMapper;

    SecurityUtils securityUtils;

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });

        boolean currentUserIsAdmin = securityUtils.userIsAdmin(Objects.requireNonNull(AuthService.getCurrentUser()));

        return currentUserIsAdmin ? userDtoMapper.mapUserEntityToDtoForAdmin(user)
                : userDtoMapper.mapUserEntityToDtoForUser(user);
    }


    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        boolean currentUserIsAdmin = securityUtils.userIsAdmin(Objects.requireNonNull(AuthService.getCurrentUser()));

        return currentUserIsAdmin ? userDtoMapper.mapListUserEntityToDtoForAdmin(users)
                : userDtoMapper.mapListUserEntityToDtoForUser(users);
    }

    public void deleteUser(Long id) {
        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });

        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());
        boolean currentUserIsAdmin = securityUtils.userIsAdmin(currentUser);

        if (currentUserIsAdmin) {
            userRepository.deleteById(id);
            log.info("delete user {} by admin {}", userFromDb.getEmail(), currentUser.getUsername());
        } else {
            if (userFromDb.getEmail().equals(currentUser.getUsername())) {
                userRepository.deleteById(id);
                log.info("delete user profile {}", userFromDb.getEmail());
            } else
                throw new SomeoneElseUserProfileException();
        }
    }

    public UserDto updateUser(Long id, UserUpdateRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });

        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());
        boolean currentUserIsAdmin = securityUtils.userIsAdmin(currentUser);

        if (user.getEmail().equals(currentUser.getUsername())) {
            if (updateRequest.getFirstName() != null)
                user.setFirstName(updateRequest.getFirstName());
            if (updateRequest.getLastName() != null)
                user.setLastName(updateRequest.getLastName());
            if (updateRequest.getEmail() != null)
                user.setEmail(updateRequest.getEmail());
            if (updateRequest.getCountry() != null)
                user.setCountry(updateRequest.getCountry());

            userRepository.save(user);
            log.info("updated user profile {}", user.getEmail());

            return currentUserIsAdmin ? userDtoMapper.mapUserEntityToDtoForAdmin(user)
                    : userDtoMapper.mapUserEntityToDtoForUser(user);
        } else
            throw new SomeoneElseUserProfileException();
    }

}
