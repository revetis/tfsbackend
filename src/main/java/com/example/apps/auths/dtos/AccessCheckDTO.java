package com.example.apps.auths.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccessCheckDTO {
    private Boolean isPermitted;
}
