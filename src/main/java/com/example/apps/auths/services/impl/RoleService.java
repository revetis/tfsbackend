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
        org.springframework.beans.BeanUtils.copyProperties(role, response);
        return response;
    }

    @Override
    public RoleDTO updateRole(Long roleId, RoleDTOIU request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        role.setName(request.getName());
        roleRepository.save(role);
        RoleDTO response = new RoleDTO();
        org.springframework.beans.BeanUtils.copyProperties(role, response);
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
        org.springframework.beans.BeanUtils.copyProperties(role, response);
        return response;

    }

    @Override
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll(org.springframework.data.domain.Sort.by("createdAt").descending());
        return roles.stream()
                .map(role -> {
                    RoleDTO roleDTO = new RoleDTO();
                    org.springframework.beans.BeanUtils.copyProperties(role, roleDTO);
                    return roleDTO;
                })
                .toList();

    }

    @Override
    public RolePageResult getAllRoles(int start, int end, String sortField, String sortOrder, String search) {
        org.springframework.data.jpa.domain.Specification<Role> spec = (root, query, cb) -> cb.conjunction();

        if (search != null && !search.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }

        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                sortOrder.equalsIgnoreCase("ASC") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC,
                sortField);

        int pageSize = end - start;
        if (pageSize <= 0)
            pageSize = 10;
        int pageNumber = start / pageSize;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber,
                pageSize, sort);

        org.springframework.data.domain.Page<Role> page = roleRepository.findAll(spec, pageable);

        List<RoleDTO> dtos = page.getContent().stream().map(role -> {
            RoleDTO roleDTO = new RoleDTO();
            org.springframework.beans.BeanUtils.copyProperties(role, roleDTO);
            return roleDTO;
        }).toList();

        return new RolePageResult(dtos, page.getTotalElements());
    }

}
