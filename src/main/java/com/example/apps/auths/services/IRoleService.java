package com.example.apps.auths.services;

import java.util.List;

import com.example.apps.auths.dtos.RoleDTO;
import com.example.apps.auths.dtos.RoleDTOIU;

public interface IRoleService {
    RoleDTO createRole(RoleDTOIU request);

    RoleDTO updateRole(Long roleId, RoleDTOIU request);

    void deleteRole(Long roleId);

    RoleDTO getRoleById(Long roleId);

    List<RoleDTO> getAllRoles();

    RolePageResult getAllRoles(int start, int end, String sortField, String sortOrder, String search);

    record RolePageResult(List<RoleDTO> data, long totalCount) {
    }
}
