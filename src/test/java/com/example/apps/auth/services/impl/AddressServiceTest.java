package com.example.apps.auth.services.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.apps.auths.dtos.AddressDTO;
import com.example.apps.auths.dtos.AddressDTOIU;
import com.example.apps.auths.entities.Address;
import com.example.apps.auths.entities.User;
import com.example.apps.auths.repositories.IAddressRepository;
import com.example.apps.auths.repositories.IUserRepository;
import com.example.apps.auths.securities.CustomUserDetails;
import com.example.apps.auths.services.impl.AddressService;
import com.example.tfs.exceptions.AddressDenied;
import com.example.tfs.exceptions.AddressNotFoundException;
import com.example.tfs.exceptions.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IAddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    private User user;
    private Address address;
    private AddressDTOIU addressRequest;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setAddresses(new ArrayList<>());

        address = new Address();
        address.setId(1L);
        address.setUser(user);
        address.setCity("Istanbul");
        address.setCountry("Turkey");

        addressRequest = new AddressDTOIU();
        addressRequest.setCity("Istanbul");
        addressRequest.setCountry("Turkey");

        customUserDetails = mock(CustomUserDetails.class);
        lenient().when(customUserDetails.getUsername()).thenReturn("testuser");
    }

    @Test
    void createAddress_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDTO result = addressService.createAddress(customUserDetails, addressRequest);

        assertNotNull(result);
        assertEquals("Istanbul", result.getCity());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createAddress_UserNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> addressService.createAddress(customUserDetails, addressRequest));
    }

    @Test
    void updateAddress_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        AddressDTOIU updateRequest = new AddressDTOIU();
        updateRequest.setCity("Ankara");

        AddressDTO result = addressService.updateAddress(customUserDetails, 1L, updateRequest);

        assertNotNull(result);
        assertEquals("Ankara", result.getCity());
    }

    @Test
    void updateAddress_AddressNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AddressNotFoundException.class,
                () -> addressService.updateAddress(customUserDetails, 1L, addressRequest));
    }

    @Test
    void updateAddress_AddressDenied() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(otherUser));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        assertThrows(AddressDenied.class, () -> addressService.updateAddress(customUserDetails, 1L, addressRequest));
    }

    @Test
    void deleteAddress_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(any(Address.class));

        addressService.deleteAddress(customUserDetails, 1L);

        verify(addressRepository).delete(address);
    }
}
