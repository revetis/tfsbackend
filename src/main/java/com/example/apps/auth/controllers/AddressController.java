package com.example.apps.auth.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.example.settings.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/rest/api/private/auth/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping(path = "/create")
    public ResponseEntity<ApiTemplate<Void, AddressDTO>> creatAddress(@AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid AddressDTOIU request, HttpServletRequest servletRequest) {
        AddressDTO address = addressService.createAddress(user, request);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, address));
    }

    @PutMapping(path = "/update/{addressId}")
    public ResponseEntity<ApiTemplate<Void, AddressDTO>> updateAddress(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AddressDTOIU request,
            @PathVariable("addressId") Long addressId, HttpServletRequest servletRequest) {
        AddressDTO address = addressService.updateAddress(user, addressId, request);
        return ResponseEntity
                .ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null, address));
    }

    @DeleteMapping(path = "/delete/{addressId}")
    public ResponseEntity<ApiTemplate<Void, String>> deleteAddress(@AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("addressId") Long addressId, HttpServletRequest servletRequest) {
        addressService.deleteAddress(user, addressId);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, servletRequest.getRequestURI(), null,
                "Address deleted successfully"));
    }
}
