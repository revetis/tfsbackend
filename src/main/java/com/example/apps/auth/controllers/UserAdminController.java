package com.example.apps.auth.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dtos.UserDTO;
import com.example.apps.auth.dtos.UserDTOCreate;
import com.example.apps.auth.dtos.UserDTOUpdate;
import com.example.apps.auth.services.IUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/admin/auth/users")
@Validated
public class UserAdminController {
    @Autowired
    private IUserService userService;

    @GetMapping(path = "/all")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping(path = "/{userId}")
    public UserDTO getUserById(@PathVariable("userId") Long userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping(path = "/delete/{userId}")
    public void deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
    }

    @PutMapping(path = "/update/{userId}")
    public UserDTO updateUser(@PathVariable("userId") Long userId, @RequestBody @Valid UserDTOUpdate request) {
        return userService.updateUser(userId, request);
    }

    @PostMapping(path = "/create")
    public UserDTO createUser(@RequestBody @Valid UserDTOCreate request) {
        return userService.createUser(request);
    }

    @PutMapping(path = "/put-user-role/{userId}/{roleId}")
    public void putUserRole(@PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId) {
        userService.putUserRole(userId, roleId);
    }

    @DeleteMapping(path = "/delete-user-role/{userId}/{roleId}")
    public void deleteUserRole(@PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId) {
        userService.deleteUserRole(userId, roleId);
    }

    @PutMapping(path = "/enable-user/{userId}")
    public void enableUser(@PathVariable("userId") Long userId) {
        userService.enableUser(userId);
    }

    @PutMapping(path = "/disable-user/{userId}")
    public void disableUser(@PathVariable("userId") Long userId) {
        userService.disableUser(userId);
    }

}
