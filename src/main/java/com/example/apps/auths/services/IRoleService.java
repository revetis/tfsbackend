package com.example.apps.auths.services;

import java.util.List;

import com.example.apps.auths.dtos.RoleDTO;
import com.example.apps.auths.dtos.RoleDTOIU;

public interface IRoleService {
    public RoleDTO createRole(RoleDTOIU request);

    public RoleDTO updateRole(Long roleId, RoleDTOIU request);

    public void deleteRole(Long roleId);

    public RoleDTO getRoleById(Long roleId);

    public List<RoleDTO> getAllRoles();

}
