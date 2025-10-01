package com.salcatech.nfc_api.controller;

import com.salcatech.nfc_api.model.Users;
import com.salcatech.nfc_api.service.UsersService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    public Users createUser(@RequestBody Users user) {
        return usersService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        usersService.deleteUser(id);
    }

    @GetMapping
    public List<Users> getAllUsers() {
        return usersService.getAllUsers();
    }
}

