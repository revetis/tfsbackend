package com.example.apps.shipments.dtos;

import lombok.Data;

@Data
public class GeliverCityResponse {
        private String name;
        private String areaCode;
        private String cityCode;
        private String countryCode;
}