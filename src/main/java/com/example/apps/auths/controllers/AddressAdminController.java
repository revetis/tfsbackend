package com.example.apps.auths.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.apps.auths.dtos.AddressAdminDTO;
import com.example.apps.auths.services.IAddressService;
import com.example.tfs.maindto.ApiTemplate;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/rest/api/admin/auth/address")
public class AddressAdminController {

    @Autowired
    private IAddressService addressService;

    @GetMapping("/all")
    public ResponseEntity<ApiTemplate<Void, List<AddressAdminDTO>>> getAllAddresses(HttpServletRequest servletRequest) {
        List<AddressAdminDTO> addresses = addressService.getAllAddresses();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                200,
                servletRequest.getRequestURI(),
                null,
                addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiTemplate<Void, AddressAdminDTO>> getAddressById(@PathVariable Long id,
            HttpServletRequest servletRequest) {
        AddressAdminDTO address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                200,
                servletRequest.getRequestURI(),
                null,
                address));
    }
}
