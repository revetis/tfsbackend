package com.example.apps.shipments.dtos;

import lombok.Data;

@Data
public class GeliverDistrictResponse {
    /** Efendim, ilçenin tam ismini (örn: Adalar) temsil eder. */
    private String name;

    /** Sistemin ilçeyi tanımak için kullandığı o eşsiz kimlik numarasıdır. */
    private Long districtID;

    /**
     * Bağlı olduğu şehrin plaka kodu; sizin sisteminizde Istanbul için "34"
     * olacaktır.
     */
    private String cityCode;

    /**
     * Bölge kodu; şu an null gelse de gelecekteki genişlemeler için yerini
     * hazırladım.
     */
    private String regionCode;

    /** Ülke kodu; Türkiye'miz için her zamanki gibi "TR". */
    private String countryCode;
}