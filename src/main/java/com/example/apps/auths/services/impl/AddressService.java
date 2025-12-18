package com.example.apps.auths.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.auths.dtos.AddressDTO;
import com.example.apps.auths.dtos.AddressDTOIU;
import com.example.apps.auths.entities.Address;
import com.example.apps.auths.entities.User;
import com.example.apps.auths.repositories.IAddressRepository;
import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.auths.securities.CustomUserDetails;
import com.example.apps.auths.services.IAddressService;
import com.example.tfs.exceptions.AddressDenied;
import com.example.tfs.exceptions.AddressNotFoundException;
import com.example.tfs.exceptions.UserNotFoundException;

@Service
public class AddressService implements IAddressService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IAddressRepository addressRepository;

    @Override
    @Transactional
    public AddressDTO createAddress(CustomUserDetails userD, AddressDTOIU request) {
        User user = userRepository.findByUsername(userD.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<>());
        }
        List<Address> addresses = user.getAddresses();

        Address address = new Address();
        BeanUtils.copyProperties(request, address);

        address.setUser(user);
        addresses.add(address);
        addressRepository.save(address);
        AddressDTO response = new AddressDTO();
        BeanUtils.copyProperties(address, response);
        return response;
    }

    @Override
    @Transactional
    public AddressDTO updateAddress(CustomUserDetails userD, Long addressId, AddressDTOIU request) {
        User user = userRepository.findByUsername(userD.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AddressDenied("Address not found for this user");
        }

        BeanUtils.copyProperties(request, address);
        addressRepository.save(address);

        AddressDTO response = new AddressDTO();
        BeanUtils.copyProperties(address, response);
        return response;
    }

    @Override
    @Transactional
    public void deleteAddress(CustomUserDetails userD, Long addressId) {
        User user = userRepository.findByUsername(userD.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AddressDenied("Address not found for this user");
        }
        addressRepository.delete(address);
    }

}
