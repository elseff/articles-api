package com.elseff.project.service.user;

import com.elseff.project.dto.user.UserAllFieldsCanBeNullDto;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.dto.user.UserDto;
import com.elseff.project.entity.User;
import com.elseff.project.enums.Role;
import com.elseff.project.exception.user.SomeoneElseUserProfileException;
import com.elseff.project.exception.user.UserNotFoundException;
import com.elseff.project.repository.UserRepository;
import com.elseff.project.service.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserRepository repository;

    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public UserAllFieldsDto getUserById(Long id) {
        return modelMapper.map(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id)), UserAllFieldsDto.class);
    }


    public List<UserDto> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        User currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        User userFromDb = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (currentUser.getRoles().contains(Role.ADMIN)) {
            repository.deleteById(id);
            log.info("delete user {} by admin {}", userFromDb.getEmail(), currentUser.getEmail());
        } else {
            if (userFromDb.equals(currentUser)) {
                repository.deleteById(id);
                log.info("delete user profile {}", userFromDb.getEmail());
            } else
                throw new SomeoneElseUserProfileException();
        }
    }

    public UserAllFieldsDto updateUser(Long id, UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto) {
        User currentUser = Objects.requireNonNull(AuthService.getCurrentUser());

        User userFromDb = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (userFromDb.equals(currentUser)) {
            if (userAllFieldsCanBeNullDto.getFirstName() != null)
                userFromDb.setFirstName(userAllFieldsCanBeNullDto.getFirstName());
            if (userAllFieldsCanBeNullDto.getLastName() != null)
                userFromDb.setLastName(userAllFieldsCanBeNullDto.getLastName());
            if (userAllFieldsCanBeNullDto.getEmail() != null) userFromDb.setEmail(userAllFieldsCanBeNullDto.getEmail());
            if (userAllFieldsCanBeNullDto.getCountry() != null)
                userFromDb.setCountry(userAllFieldsCanBeNullDto.getCountry());
            repository.save(userFromDb);
            log.info("updated user profile {}", userFromDb.getEmail());
            return modelMapper.map(userFromDb, UserAllFieldsDto.class);
        } else
            throw new SomeoneElseUserProfileException();
    }

}
