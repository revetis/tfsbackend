package com.example.apps.auth.services;

import com.example.apps.auth.dtos.AddressDTO;
import com.example.apps.auth.dtos.AddressDTOIU;
import com.example.apps.auth.securities.CustomUserDetails;

public interface IAddressService {
    public AddressDTO createAddress(CustomUserDetails user, AddressDTOIU request);

    public AddressDTO updateAddress(CustomUserDetails user, Long addressId, AddressDTOIU request);

    public void deleteAddress(CustomUserDetails user, Long addressId);

}
