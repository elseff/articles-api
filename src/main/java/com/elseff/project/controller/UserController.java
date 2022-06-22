package com.elseff.project.controller;

import com.elseff.project.dto.user.UserAllFieldsDto;
import com.elseff.project.dto.user.UserDto;
import com.elseff.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://192.168.100.3:4200", "http://localhost:4200"})
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping()
    public UserAllFieldsDto addUser(@RequestBody @Valid UserAllFieldsDto userAllFieldsDto) {
        return userService.addUser(userAllFieldsDto);
    }

    @GetMapping("/{id}")
    public UserAllFieldsDto getSpecific(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }
}
