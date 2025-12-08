package com.example.apps.auth.services;

import com.example.apps.auth.dto.LoginRequestDTO;
import com.example.apps.auth.dto.LoginRequestDTOIU;

public interface ILoginService {
    LoginRequestDTO loginService(LoginRequestDTOIU request) throws Exception;
}
