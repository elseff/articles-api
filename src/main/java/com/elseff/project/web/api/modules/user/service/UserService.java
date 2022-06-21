package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.exception.IdLessThanZeroException;
import com.elseff.project.exception.UserNotFoundException;
import com.elseff.project.persistence.User;
import com.elseff.project.persistence.dao.UserDao;
import com.elseff.project.web.api.modules.user.dto.UserAllFieldsDto;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserDao dao;

    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserDao dao, ModelMapper modelMapper) {
        this.dao = dao;
        this.modelMapper = modelMapper;
    }

    public UserAllFieldsDto getUserById(Long id) {
        validate(id);
        return modelMapper.map(dao.findById(id).get(), UserAllFieldsDto.class);
    }


    public List<UserDto> getAllUsers() {
        return dao.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
    }

    public UserAllFieldsDto addUser(UserAllFieldsDto userAllFieldsDto) {
        User user = modelMapper.map(userAllFieldsDto, User.class);
        User userFromDb = dao.save(user);
        return modelMapper.map(userFromDb, UserAllFieldsDto.class);
    }

    public void deleteUser(Long id) {
        validate(id);
        dao.deleteById(id);
    }

    //throws exception if id less than zero or if user not found
    private void validate(Long id) {
        if (id < 0) {
            log.debug("[-] id {} is less 0", id);
            throw new IdLessThanZeroException();
        }
        if (dao.findById(id).isEmpty()) {
            log.debug("[-] user with id {} not found", id);
            throw new UserNotFoundException(id);
        }
    }
}
