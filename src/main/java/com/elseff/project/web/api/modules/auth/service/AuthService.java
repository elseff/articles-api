package com.elseff.project.web.api.modules.auth.service;

import com.elseff.project.persistense.Role;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.auth.dto.AuthRegisterRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthLoginRequest;
import com.elseff.project.web.api.modules.auth.dto.AuthResponse;
import com.elseff.project.web.api.modules.auth.exception.AuthUserNotFoundException;
import com.elseff.project.web.api.modules.auth.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@Validated
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(@Valid AuthRegisterRequest authRegisterRequest) {
        if (userRepository.existsByEmail(authRegisterRequest.getEmail())) {
            log.warn("User with email " + authRegisterRequest.getEmail() + " already exists");
            throw new AuthenticationException("User with email " + authRegisterRequest.getEmail() + " already exists");
        }

        User user = modelMapper.map(authRegisterRequest, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.getByName("ROLE_USER");

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        user.setRoles(roles);
        User userFromDb = userRepository.save(user);
        String token = Base64.encodeBase64String(String.format("%s:%s",
                userFromDb.getEmail(), userFromDb.getPassword()).getBytes(StandardCharsets.UTF_8));
        log.info("User with email {} has been successfully registered", userFromDb.getEmail());

        return new AuthResponse(userFromDb.getId(), authRegisterRequest.getEmail(), token);
    }

    public AuthResponse login(@Valid AuthLoginRequest authLoginRequest) {
        if (!userRepository.existsByEmail(authLoginRequest.getEmail())) {
            log.warn("User with email {} is not found", authLoginRequest.getEmail());
            throw new AuthUserNotFoundException("User with email " + authLoginRequest.getEmail() + " is not found");
        } else {
            User userFromDb = userRepository.getByEmail(authLoginRequest.getEmail());
            if (!passwordEncoder.matches(authLoginRequest.getPassword(), userFromDb.getPassword())) {
                log.warn("Incorrect password");
                throw new AuthenticationException("Incorrect password");
            } else {
                String token = Base64.encodeBase64String(String.format("%s:%s",
                        authLoginRequest.getEmail(), authLoginRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
                log.info("User with email {} has been successfully login", authLoginRequest.getEmail());
                return new AuthResponse(userFromDb.getId(), authLoginRequest.getEmail(), token);
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
