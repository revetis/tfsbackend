package com.example.apps.shipments.services;

import com.example.apps.shipments.dtos.GeliverCityListResponse;
import com.example.apps.shipments.dtos.GeliverDistrictListResponse;
import com.example.apps.shipments.dtos.GeliverMainResponse;
import com.example.apps.shipments.dtos.GeliverShipmentCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionMainResponse;
import com.example.apps.shipments.dtos.GeliverWebhookRequest;
import com.example.apps.shipments.dtos.GeliverReturnRequest;
import com.example.apps.shipments.dtos.ShipmentDTO;
import com.example.apps.shipments.dtos.ShipmentEventDTO;

import javax.resource.NotSupportedException;
import java.util.List;

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

    /** Initiates a return shipment creation. */
    GeliverTransactionMainResponse createReturnShipment(String originalShipmentId, GeliverReturnRequest request);

    /** Completes the purchase transaction for a specific shipping offer. */
    GeliverTransactionMainResponse offerPurchase(GeliverTransactionCreateRequest request);

    /** Cancel shipment based on the provided shipment ID. */
    GeliverMainResponse cancelShipmentByID(String shipmentID);

    /** Cancel shipment based on the provided order number. */
    GeliverMainResponse cancelShipment(String orderNumber) throws NotSupportedException;

    /** Lists shipment details based on the provided order number. */
    GeliverMainResponse listShipment(String orderNumber);

    List<ShipmentDTO> getAllShipments();

    // Paginated version with filtering
    ShipmentPageResult getAllShipments(int page, int size, String sortField, String sortOrder, String search,
            String status, Long userId);

    ShipmentDTO getShipmentById(Long id);

    /** Processes incoming tracking webhook from Geliver. */
    void processTrackingWebhook(GeliverWebhookRequest request);

    /** Gets all events for a shipment by shipment ID. */
    List<ShipmentEventDTO> getShipmentEvents(Long shipmentId);

    /** Gets all events for a shipment by order number. */
    List<ShipmentEventDTO> getShipmentEventsByOrderNumber(String orderNumber);

    /** Retrieves shipment details by order number. Returns null if not found. */
    ShipmentDTO getShipmentByOrderNumber(String orderNumber);

    void deleteShipmentById(Long id);

    // Result record for paginated shipments
    record ShipmentPageResult(List<ShipmentDTO> data, long totalCount) {
    }
}