package com.example.apps.auth.services;

import com.example.apps.auth.dto.UserLoginDTO;
import com.example.apps.auth.dto.UserLoginDTOIU;
import com.example.apps.auth.dto.UserRegisterDTO;
import com.example.apps.auth.dto.UserRegisterDTOIU;

public interface IUserService {
    public UserRegisterDTO registerUser(UserRegisterDTOIU request);

    public UserLoginDTO login(UserLoginDTOIU request);
}
