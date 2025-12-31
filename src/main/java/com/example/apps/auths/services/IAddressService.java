package com.example.apps.auths.services;

import com.example.apps.auths.dtos.AddressDTO;
import com.example.apps.auths.dtos.AddressDTOIU;
import com.example.apps.auths.securities.CustomUserDetails;

import java.util.List;
import com.example.apps.auths.dtos.AddressAdminDTO;

public interface IAddressService {
    public AddressDTO createAddress(CustomUserDetails user, AddressDTOIU request);

    public AddressDTO updateAddress(CustomUserDetails user, Long addressId, AddressDTOIU request);

    public void deleteAddress(CustomUserDetails user, Long addressId);

    public List<AddressAdminDTO> getAllAddresses();

    public AddressAdminDTO getAddressById(Long id);
}
