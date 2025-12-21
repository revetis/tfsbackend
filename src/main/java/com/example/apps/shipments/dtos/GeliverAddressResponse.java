package com.example.apps.shipments.dtos;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class GeliverAddressResponse {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String shortName;
    private String name;
    private String email;
    private String phone;
    private String address1;
    private String address2;
    private String cityCode;
    private String cityName;
    private String zip;
    private Long districtID;
    private String districtName;
    private String countryCode;
    private String countryName;
    private boolean isDefaultSenderAddress;
    private boolean isDefaultReturnAddress;
    private boolean isRecipientAddress;
    private boolean isActive;
}