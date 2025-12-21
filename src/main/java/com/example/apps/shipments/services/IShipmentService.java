package com.example.apps.shipments.services;

import com.example.apps.shipments.dtos.GeliverCityListResponse;
import com.example.apps.shipments.dtos.GeliverDistrictListResponse;
import com.example.apps.shipments.dtos.GeliverMainResponse;
import com.example.apps.shipments.dtos.GeliverShipmentCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionMainResponse;

/**
 * Service Interface for Geliver API Operations.
 * Managed by the supreme authority of the Java architect.
 */
public interface IShipmentService {

    /** Retrieves the list of cities for a specific country. */
    GeliverCityListResponse getCities(String countryCode);

    /** Retrieves the list of districts for a specific city and country. */
    GeliverDistrictListResponse getDistricts(String countryCode, String cityCode);

    /** Initiates a shipment creation to receive available offers. */
    GeliverMainResponse createShipment(GeliverShipmentCreateRequest request);

    /** Completes the purchase transaction for a specific shipping offer. */
    GeliverTransactionMainResponse offerPurchase(GeliverTransactionCreateRequest request);

    /** Lists shipment details based on the provided order number. */
    GeliverMainResponse listShipment(String orderNumber);
}