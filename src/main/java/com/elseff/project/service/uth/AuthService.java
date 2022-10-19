package com.elseff.project.service.uth;

import com.elseff.project.enums.Role;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.entity.User;
import com.elseff.project.exception.AuthenticationException;
import com.elseff.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Slf4j
@Service
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

    public UserAllFieldsDto register(UserAllFieldsDto userAllFieldsDto) {
        if (repository.existsByEmail(userAllFieldsDto.getEmail())){
            throw new AuthenticationException("User with email " + userAllFieldsDto.getEmail() + " already exists");
        }

        User user = modelMapper.map(userAllFieldsDto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.USER);
        User userFromDb = repository.save(user);
        return modelMapper.map(userFromDb, UserAllFieldsDto.class);
    }
}
