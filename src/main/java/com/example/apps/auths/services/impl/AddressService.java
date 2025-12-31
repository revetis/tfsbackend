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
import com.example.apps.auths.dtos.AddressAdminDTO;

import com.example.apps.auths.repositories.IAddressRepository;
import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.auths.securities.CustomUserDetails;
import com.example.apps.auths.services.IAddressService;
import com.example.tfs.exceptions.AddressDenied;
import com.example.tfs.exceptions.AddressNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import com.example.tfs.exceptions.UserNotFoundException;

@Service
public class AddressService implements IAddressService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IAddressRepository addressRepository;

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
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
    @CacheEvict(value = "users", allEntries = true)
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
    @CacheEvict(value = "users", allEntries = true)
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

    @Override
    public List<AddressAdminDTO> getAllAddresses() {
        List<Address> addresses = addressRepository
                .findAll(org.springframework.data.domain.Sort.by("createdAt").descending());
        List<AddressAdminDTO> dtos = new ArrayList<>();
        for (Address address : addresses) {
            AddressAdminDTO dto = new AddressAdminDTO();
            BeanUtils.copyProperties(address, dto);
            if (address.getUser() != null) {
                dto.setUsername(address.getUser().getUsername());
                dto.setUserEmail(address.getUser().getEmail());
            }
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public AddressAdminDTO getAddressById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Address not found"));
        AddressAdminDTO dto = new AddressAdminDTO();
        BeanUtils.copyProperties(address, dto);
        if (address.getUser() != null) {
            dto.setUsername(address.getUser().getUsername());
            dto.setUserEmail(address.getUser().getEmail());
        }
        return dto;
    }
}
