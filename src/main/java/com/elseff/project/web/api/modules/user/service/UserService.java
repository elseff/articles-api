package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistense.dao.RoleRepository;
import com.elseff.project.web.api.modules.user.dto.UserAllFieldsCanBeNullDto;
import com.elseff.project.web.api.modules.user.dto.UserAllFieldsDto;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.persistense.User;
import com.elseff.project.persistense.Role;
import com.elseff.project.web.api.modules.user.exception.SomeoneElseUserProfileException;
import com.elseff.project.web.api.modules.user.exception.UserNotFoundException;
import com.elseff.project.persistense.dao.UserRepository;
import com.elseff.project.web.api.modules.auth.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }

    public UserAllFieldsDto getUserById(Long id) {
        return modelMapper.map(userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                }), UserAllFieldsDto.class);
    }


    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });
        Role roleAdmin = roleRepository.getByName("ROLE_ADMIN");
        if (currentUser.getAuthorities().contains(roleAdmin)) {
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

    public UserAllFieldsDto updateUser(Long id, UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto) {
        UserDetails currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        User userFromDb = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("could not find user " + id);
                    return new UserNotFoundException("could not find user " + id);
                });
        if (userFromDb.getEmail().equals(currentUser.getUsername())) {
            if (userAllFieldsCanBeNullDto.getFirstName() != null)
                userFromDb.setFirstName(userAllFieldsCanBeNullDto.getFirstName());
            if (userAllFieldsCanBeNullDto.getLastName() != null)
                userFromDb.setLastName(userAllFieldsCanBeNullDto.getLastName());
            if (userAllFieldsCanBeNullDto.getEmail() != null) userFromDb.setEmail(userAllFieldsCanBeNullDto.getEmail());
            if (userAllFieldsCanBeNullDto.getCountry() != null)
                userFromDb.setCountry(userAllFieldsCanBeNullDto.getCountry());
            userRepository.save(userFromDb);
            log.info("updated user profile {}", userFromDb.getEmail());
            return modelMapper.map(userFromDb, UserAllFieldsDto.class);
        } else
            throw new SomeoneElseUserProfileException();
    }

}
