package com.example.apps.auths.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/admin/auth/roles")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    @PostMapping(path = "/create")
    public ResponseEntity<ApiTemplate<Void, RoleDTO>> createRole(@RequestBody @Valid RoleDTOIU request,
            HttpServletRequest servletRequest) {
        RoleDTO role = roleService.createRole(request);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, role));
    }

    @PutMapping(path = "/update/{roleId}")
    public ResponseEntity<ApiTemplate<Void, RoleDTO>> updateRole(@PathVariable("roleId") Long roleId,
            @RequestBody @Valid RoleDTOIU request, HttpServletRequest servletRequest) {
        RoleDTO role = roleService.updateRole(roleId, request);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, role));
    }

    @DeleteMapping(path = "/delete/{roleId}")
    public ResponseEntity<ApiTemplate<Void, String>> deleteRole(@PathVariable("roleId") Long roleId,
            HttpServletRequest servletRequest) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                "Role deleted successfully"));
    }

    @GetMapping(path = "/{roleId}")
    public ResponseEntity<ApiTemplate<Void, RoleDTO>> getRole(@PathVariable("roleId") Long roleId,
            HttpServletRequest servletRequest) {
        RoleDTO role = roleService.getRoleById(roleId);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, role));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<ApiTemplate<Void, List<RoleDTO>>> getAllRoles(HttpServletRequest servletRequest) {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, roles));
    }

}
