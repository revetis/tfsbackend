package com.example.apps.auth.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.apps.auth.dtos.RoleDTO;
import com.example.apps.auth.dtos.RoleDTOIU;
import com.example.apps.auth.entities.Role;
import com.example.apps.auth.repositories.IRoleRepository;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private IRoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private RoleDTOIU roleRequest;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        roleRequest = new RoleDTOIU();
        roleRequest.setName("ADMIN");
    }

    @Test
    void createRole_Success() {
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleDTO result = roleService.createRole(roleRequest);

        assertNotNull(result);
        assertEquals("ADMIN", result.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void getRoleById_Success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));

        RoleDTO result = roleService.getRoleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ADMIN", result.getName());
    }

    @Test
    void getRoleById_NotFound() {
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roleService.getRoleById(1L));
    }

    @Test
    void updateRole_Success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);

        RoleDTOIU updateRequest = new RoleDTOIU();
        updateRequest.setName("SUPER_ADMIN");

        RoleDTO result = roleService.updateRole(1L, updateRequest);

        assertNotNull(result);
        assertEquals("SUPER_ADMIN", result.getName());
    }

    @Test
    void deleteRole_Success() {
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        doNothing().when(roleRepository).delete(any(Role.class));

        roleService.deleteRole(1L);

        verify(roleRepository).delete(role);
    }

    @Test
    void getAllRoles_Success() {
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<RoleDTO> result = roleService.getAllRoles();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getName());
    }
}
