package com.elseff.project.service;

import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.dto.user.UserDto;
import com.elseff.project.entity.User;
import com.elseff.project.exception.IdLessThanZeroException;
import com.elseff.project.exception.NotFoundException;
import com.elseff.project.exception.UserNotFoundException;
import com.elseff.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.elseff.project.validation.CustomValidator.isNumeric;

@Slf4j
@Service
public class UserService {

    private final UserRepository repository;

    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository dao, ModelMapper modelMapper) {
        this.repository = dao;
        this.modelMapper = modelMapper;
    }

    public UserAllFieldsDto getUserById(String stringId) {
        Long id = validate(stringId);
        return modelMapper.map(repository.findById(id).get(), UserAllFieldsDto.class);
    }


    public List<UserDto> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    public UserAllFieldsDto addUser(UserAllFieldsDto userAllFieldsDto) {
        User user = modelMapper.map(userAllFieldsDto, User.class);
        User userFromDb = repository.save(user);
        return modelMapper.map(userFromDb, UserAllFieldsDto.class);
    }

    public void deleteUser(String stringId) {
        Long id = validate(stringId);
        repository.deleteById(id);
    }

    private Long validate(String stringId) {
        if (!isNumeric(stringId)) {
            log.debug("[-] {} isn't a number", stringId);
            throw new NotFoundException(stringId);
        }
        Long id = Long.parseLong(stringId);
        if (id < 0) {
            log.debug("[-] id {} is less 0", id);
            throw new IdLessThanZeroException();
        }
        if (repository.findById(id).isEmpty()) {
            log.debug("[-] user with id {} not found", id);
            throw new UserNotFoundException(id);
        }
        return id;
    }
}
