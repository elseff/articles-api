package com.elseff.project.service.auth;

import com.elseff.project.dto.auth.AuthRequest;
import com.elseff.project.dto.auth.AuthResponse;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.entity.User;
import com.elseff.project.enums.Role;
import com.elseff.project.exception.AuthenticationException;
import com.elseff.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Slf4j
@Service
@Validated
public class AuthService {
    private final UserRepository repository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository repository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(@Valid UserAllFieldsDto userAllFieldsDto) {
        if (repository.existsByEmail(userAllFieldsDto.getEmail())) {
            throw new AuthenticationException("User with email " + userAllFieldsDto.getEmail() + " already exists");
        }

        User user = modelMapper.map(userAllFieldsDto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.USER);
        User userFromDb = repository.save(user);
        String token = Base64.encodeBase64String(String.format("%s:%s",
                userAllFieldsDto.getEmail(), userAllFieldsDto.getPassword()).getBytes(StandardCharsets.UTF_8));
        log.info("User with email {} has been successfully registered", userFromDb.getEmail());

        return new AuthResponse(userAllFieldsDto.getEmail(),token);
    }

    public AuthResponse login(@Valid AuthRequest authRequest) {
        if (!repository.existsByEmail(authRequest.getEmail())) {
            log.warn("User with email {} is not found", authRequest.getEmail());
            throw new AuthenticationException("User with email " + authRequest.getEmail() + " is not found");
        } else {
            User userFromDb = repository.findByEmail(authRequest.getEmail());
            if (!passwordEncoder.matches(authRequest.getPassword(), userFromDb.getPassword())) {
                log.warn("Incorrect password");
                throw new AuthenticationException("Incorrect password");
            } else {
                String token = Base64.encodeBase64String(String.format("%s:%s",
                        authRequest.getEmail(), authRequest.getPassword()).getBytes(StandardCharsets.UTF_8));
                log.info("User with email {} has been successfully login", authRequest.getEmail());
                return new AuthResponse(authRequest.getEmail(),token);
            }
        }
    }
}
