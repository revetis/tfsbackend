package com.example.apps.auths.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.apps.auths.dtos.RoleDTO;
import com.example.apps.auths.dtos.RoleDTOIU;
import com.example.apps.auths.entities.Role;
import com.example.apps.auths.repositories.IRoleRepository;
import com.example.apps.auths.services.IRoleService;

@Service
public class RoleService implements IRoleService {
    @Autowired
    IRoleRepository roleRepository;

    @Override
    public RoleDTO createRole(RoleDTOIU request) {
        Role role = new Role();
        role.setName(request.getName());

        roleRepository.save(role);
        RoleDTO response = new RoleDTO();
        response.setId(role.getId());
        response.setName(role.getName());
        return response;
    }

    @Override
    public RoleDTO updateRole(Long roleId, RoleDTOIU request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(request.getName());
        roleRepository.save(role);
        RoleDTO response = new RoleDTO();
        response.setId(role.getId());
        response.setName(role.getName());
        return response;

    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    @Override
    public RoleDTO getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        RoleDTO response = new RoleDTO();
        response.setId(role.getId());
        response.setName(role.getName());
        return response;

    }

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> {
                    RoleDTO roleDTO = new RoleDTO();
                    roleDTO.setId(role.getId());
                    roleDTO.setName(role.getName());
                    return roleDTO;
                })
                .toList();

    }

}
