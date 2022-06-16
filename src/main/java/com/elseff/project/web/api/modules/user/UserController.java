package com.elseff.project.web.api.modules.user;

import com.elseff.project.web.api.modules.user.dto.UserDto;
import com.elseff.project.web.api.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

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
    public UserDto addUser(@RequestBody @Valid UserDto userDto){
        System.out.println(userDto.toString());
        return userService.addUser(userDto);
    }

    @GetMapping("/{id}")
    public UserDto getSpecific(@PathVariable @Positive(message = "id must be greater than 0") Long id){
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable @Positive(message = "id must be greater than 0") Long id){
        userService.deleteUser(id);
    }
}
