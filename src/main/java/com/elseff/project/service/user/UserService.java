package com.elseff.project.service.user;

import com.elseff.project.dto.user.UserAllFieldsCanBeNullDto;
import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.dto.user.UserDto;
import com.elseff.project.entity.User;
import com.elseff.project.exception.IdLessThanZeroException;
import com.elseff.project.exception.user.UserNotFoundException;
import com.elseff.project.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (id < 0) {
            throw new IdLessThanZeroException();
        }
        if (repository.existsById(id)) {
            return modelMapper.map(repository.findById(id).get(), UserAllFieldsDto.class);
        } else {
            throw new UserNotFoundException(id);
        }
    }


    public List<UserDto> getAllUsers() {
        return repository.findAll()
                .stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        if (id < 0) {
            throw new IdLessThanZeroException();
        }
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new UserNotFoundException(id);
        }
    }

    public UserAllFieldsDto updateUser(Long id, UserAllFieldsCanBeNullDto userAllFieldsCanBeNullDto) {
        User userFromDb = repository.getById(id);
        if (userAllFieldsCanBeNullDto.getFirstName() != null)
            userFromDb.setFirstName(userAllFieldsCanBeNullDto.getFirstName());
        if (userAllFieldsCanBeNullDto.getLastName() != null)
            userFromDb.setLastName(userAllFieldsCanBeNullDto.getLastName());
        if (userAllFieldsCanBeNullDto.getEmail() != null) userFromDb.setEmail(userAllFieldsCanBeNullDto.getEmail());
        if (userAllFieldsCanBeNullDto.getCountry() != null)
            userFromDb.setCountry(userAllFieldsCanBeNullDto.getCountry());
        repository.save(userFromDb);
        return modelMapper.map(userFromDb, UserAllFieldsDto.class);
    }

}
