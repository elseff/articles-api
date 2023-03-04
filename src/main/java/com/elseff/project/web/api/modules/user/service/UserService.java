package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistense.UserEntity;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.security.SecurityUtils;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import com.elseff.project.web.api.modules.user.dto.UserUpdateRequest;
import com.elseff.project.web.api.modules.user.exception.SomeoneElseUserProfileException;
import com.elseff.project.web.api.modules.user.exception.UserNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;

    SecurityUtils securityUtils;

    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        UserEntity userFromDb = userRepository.findById(id)
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

    public UserEntity updateUser(Long id, UserUpdateRequest updateRequest) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });

        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        if (user.getEmail().equals(currentUser.getUsername())) {
            if (updateRequest.getFirstName() != null)
                user.setFirstName(updateRequest.getFirstName());
            if (updateRequest.getLastName() != null)
                user.setLastName(updateRequest.getLastName());
            if (updateRequest.getEmail() != null)
                user.setEmail(updateRequest.getEmail());
            if (updateRequest.getCountry() != null)
                user.setCountry(updateRequest.getCountry());

            user.setUpdatedAt(Timestamp.from(Instant.now()));
            userRepository.save(user);
            log.info("updated user profile {}", user.getEmail());

            return user;
        } else
            throw new SomeoneElseUserProfileException();
    }

    public UserEntity getMe() {
        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());
        return userRepository.getByEmail(currentUser.getUsername());
    }
}
