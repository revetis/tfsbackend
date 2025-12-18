package com.example.apps.auths.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auths.dtos.UserDTO;
import com.example.apps.auths.dtos.UserDTOCreate;
import com.example.apps.auths.dtos.UserDTOUpdate;
import com.example.apps.auths.services.IUserService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/admin/auth/users")
@Validated
public class UserAdminController {
        @Autowired
        private IUserService userService;

        @GetMapping(path = "/all")
        public ResponseEntity<ApiTemplate<Void, List<UserDTO>>> getAllUsers(HttpServletRequest servletRequest) {
                List<UserDTO> users = userService.getAllUsers();
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                users));
        }

        @GetMapping(path = "/{userId}")
        public ResponseEntity<ApiTemplate<Void, UserDTO>> getUserById(@PathVariable("userId") Long userId,
                        HttpServletRequest servletRequest) {
                UserDTO user = userService.getUserById(userId);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(),
                                null, user));
        }

        @DeleteMapping(path = "/delete/{userId}")
        public ResponseEntity<ApiTemplate<Void, String>> deleteUser(@PathVariable("userId") Long userId,
                        HttpServletRequest servletRequest) {
                userService.deleteUser(userId);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "User deleted successfully."));
        }

        @PutMapping(path = "/update/{userId}")
        public ResponseEntity<ApiTemplate<Void, UserDTO>> updateUser(@PathVariable("userId") Long userId,
                        @RequestBody @Valid UserDTOUpdate request, HttpServletRequest servletRequest) {
                UserDTO updatedUser = userService.updateUser(userId, request);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                updatedUser));
        }

        @PostMapping(path = "/create")
        public ResponseEntity<ApiTemplate<Void, UserDTO>> createUser(@RequestBody @Valid UserDTOCreate request,
                        HttpServletRequest servletRequest) {
                UserDTO createdUser = userService.createUser(request);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                createdUser));
        }

        @PutMapping(path = "/put-user-role/{userId}/{roleId}")
        public ResponseEntity<ApiTemplate<Void, String>> putUserRole(@PathVariable("userId") Long userId,
                        @PathVariable("roleId") Long roleId, HttpServletRequest servletRequest) {
                userService.putUserRole(userId, roleId);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "User role added successfully."));
        }

        @DeleteMapping(path = "/delete-user-role/{userId}/{roleId}")
        public ResponseEntity<ApiTemplate<Void, String>> deleteUserRole(@PathVariable("userId") Long userId,
                        @PathVariable("roleId") Long roleId, HttpServletRequest servletRequest) {
                userService.deleteUserRole(userId, roleId);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "User role deleted successfully."));
        }

        @PutMapping(path = "/enable-user/{userId}")
        public ResponseEntity<ApiTemplate<Void, String>> enableUser(@PathVariable("userId") Long userId,
                        HttpServletRequest servletRequest) {
                userService.enableUser(userId);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "User enabled successfully."));
        }

        @PutMapping(path = "/disable-user/{userId}")
        public ResponseEntity<ApiTemplate<Void, String>> disableUser(@PathVariable("userId") Long userId,
                        HttpServletRequest servletRequest) {
                userService.disableUser(userId);
                return ResponseEntity
                                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                                                "User disabled successfully."));
        }

}
