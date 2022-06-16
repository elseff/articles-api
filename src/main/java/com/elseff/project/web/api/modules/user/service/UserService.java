package com.elseff.project.web.api.modules.user.service;

import com.elseff.project.persistence.User;
import com.elseff.project.persistence.dao.UserDao;
import com.elseff.project.web.api.modules.user.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserDao dao;

    private final ModelMapper modelMapper = new ModelMapper();

    @Autowired
    public UserService(UserDao dao) {
        this.dao = dao;
    }

    public UserDto getUserById(Long id){
        return modelMapper.map(dao.findById(id).orElse(null),UserDto.class);
    }

    public boolean existsByEmail(String email){
        return dao.existsByEmail(email);
    }

    public List<UserDto> getAllUsers(){
        return dao.findAll().stream()
                .map(user -> modelMapper.map(user, UserDto.class)).collect(Collectors.toList());
    }

    public UserDto addUser(UserDto userDto){
        User user = modelMapper.map(userDto,User.class);
        dao.save(user);
        return userDto;
    }
    public void deleteUser(Long id){
        dao.deleteById(id);
    }
}
