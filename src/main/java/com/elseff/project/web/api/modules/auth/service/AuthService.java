package com.elseff.project.web.api.modules.auth.service;

import com.elseff.project.persistense.RoleEntity;
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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {

    UserRepository userRepository;

    RoleRepository roleRepository;

    UserDtoMapper userDtoMapper;

    PasswordEncoder passwordEncoder;

    JwtProvider jwtProvider;

    public AuthResponse register(AuthRegisterRequest authRegisterRequest) {
        if (userRepository.existsByEmail(authRegisterRequest.getEmail())) {
            log.warn("User with email " + authRegisterRequest.getEmail() + " already exists");
            throw new AuthenticationException("User with email " + authRegisterRequest.getEmail() + " already exists");
        }
        RoleEntity roleUser = roleRepository.getByName("ROLE_USER");
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(roleUser);

        User user = userDtoMapper.mapAuthRequestToUserEntity(authRegisterRequest);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);

        User userFromDb = userRepository.save(user);
        log.info("User with email {} has been successfully registered", userFromDb.getEmail());
        String token = jwtProvider.generateToken(user.getEmail());

        return new AuthResponse(userFromDb.getId(), authRegisterRequest.getEmail(), token);
    }

    public AuthResponse login(AuthLoginRequest authLoginRequest) {
        boolean existsByEmail = userRepository.existsByEmail(authLoginRequest.getEmail());

        if (!existsByEmail) {
            log.warn("User with email {} is not found", authLoginRequest.getEmail());
            throw new AuthUserNotFoundException("User with email " + authLoginRequest.getEmail() + " is not found");
        } else {
            User userFromDb = userRepository.getByEmail(authLoginRequest.getEmail());

            String loginRequestPassword = authLoginRequest.getPassword();
            String email = authLoginRequest.getEmail();
            String actualUserPassword = userFromDb.getPassword();

            if (!passwordEncoder.matches(loginRequestPassword, actualUserPassword)) {
                log.info("Incorrect password!");
                throw new AuthenticationException("Incorrect password");
            } else {
                String token = jwtProvider.generateToken(email);
                log.info("User with email {} has been successfully login", email);
                return new AuthResponse(userFromDb.getId(), email, token);
            }
        }
    }

    public static UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            return null;
        UserDetails user = (UserDetails) authentication.getPrincipal();
        return user;
    }
}
