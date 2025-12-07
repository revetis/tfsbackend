package com.example.apps.auth.services;


import com.example.apps.auth.dto.RegisterRequestDTO;
import com.example.apps.auth.dto.RegisterRequestDTOIU;

public interface IRegisterService {
    public RegisterRequestDTO register(RegisterRequestDTOIU request);
}
