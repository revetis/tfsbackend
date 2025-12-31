package com.example.apps.auths.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {
    private Long id;
    private String name;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
