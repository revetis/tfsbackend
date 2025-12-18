package com.example.apps.auths.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auths.dtos.RoleDTO;
import com.example.apps.auths.dtos.RoleDTOIU;
import com.example.apps.auths.services.IRoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/admin/auth/roles")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    @PostMapping(path = "/create")
    public RoleDTO createRole(@RequestBody @Valid RoleDTOIU request) {
        return roleService.createRole(request);
    }

    @PutMapping(path = "/update/{roleId}")
    public RoleDTO updateRole(@PathVariable("roleId") Long roleId, @RequestBody @Valid RoleDTOIU request) {
        return roleService.updateRole(roleId, request);
    }

    @DeleteMapping(path = "/delete/{roleId}")
    public void deleteRole(@PathVariable("roleId") Long roleId) {
        roleService.deleteRole(roleId);
    }

    @GetMapping(path = "/{roleId}")
    public RoleDTO getRole(@PathVariable("roleId") Long roleId) {
        return roleService.getRoleById(roleId);
    }

    @GetMapping(path = "/all")
    public List<RoleDTO> getAllRoles() {
        return roleService.getAllRoles();
    }

}
