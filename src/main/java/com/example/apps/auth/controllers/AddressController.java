package com.example.apps.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auth.dtos.AddressDTO;
import com.example.apps.auth.dtos.AddressDTOIU;
import com.example.apps.auth.securities.CustomUserDetails;
import com.example.apps.auth.services.impl.AddressService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/private/auth/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping(path = "/create")
    public AddressDTO creatAddress(@AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid AddressDTOIU request) {
        return addressService.createAddress(user, request);

    }

    @PutMapping(path = "/update/{addressId}")
    public AddressDTO updateAddress(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AddressDTOIU request,
            @PathVariable("addressId") Long addressId) {
        return addressService.updateAddress(user, addressId, request);

    }

    @DeleteMapping(path = "/delete/{addressId}")
    public void deleteAddress(@AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("addressId") Long addressId) {
        addressService.deleteAddress(user, addressId);
    }
}
