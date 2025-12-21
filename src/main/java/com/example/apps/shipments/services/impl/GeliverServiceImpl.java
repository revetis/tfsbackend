package com.example.apps.shipments.services.impl;

import com.example.apps.shipments.Configurations.GeliverConfiguration;
import com.example.apps.shipments.dtos.GeliverCityListResponse;
import com.example.apps.shipments.dtos.GeliverDistrictListResponse;
import com.example.apps.shipments.dtos.GeliverMainResponse;
import com.example.apps.shipments.dtos.GeliverShipmentCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionMainResponse;
import com.example.apps.shipments.exceptions.ShipmentException;
import com.example.apps.shipments.services.IShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class GeliverServiceImpl implements IShipmentService {

    private final WebClient geliverWebClient;
    private final GeliverConfiguration geliverConfiguration;

    public GeliverServiceImpl(WebClient geliverWebClient, GeliverConfiguration geliverConfiguration) {
        this.geliverWebClient = geliverWebClient;
        this.geliverConfiguration = geliverConfiguration;
    }

    @Override
    public GeliverCityListResponse getCities(String countryCode) {
        log.info("Fetching cities for country: {}", countryCode);
        return geliverWebClient.get()
                .uri(uri -> uri.path("/cities").queryParam("countryCode", countryCode).build())
                .retrieve()
                .bodyToMono(GeliverCityListResponse.class)
                .block();
    }

    @Override
    public GeliverDistrictListResponse getDistricts(String countryCode, String cityCode) {
        if (cityCode == null || cityCode.isBlank()) {
            throw new ShipmentException("City code cannot be null or empty.");
        }

        log.info("Fetching districts for city code: {} in country: {}", cityCode, countryCode);
        return geliverWebClient.get()
                .uri(uri -> uri.path("/districts")
                        .queryParam("countryCode", countryCode)
                        .queryParam("cityCode", cityCode).build())
                .retrieve()
                .bodyToMono(GeliverDistrictListResponse.class)
                .block();
    }

    @Override
    public GeliverMainResponse createShipment(GeliverShipmentCreateRequest incomingRequest) {
        log.info("Creating shipment request for order: {}", incomingRequest.getOrder().getOrderNumber());

        // Enriching request with configuration parameters
        incomingRequest.setTest(Boolean.parseBoolean(geliverConfiguration.getTestMode()));
        incomingRequest.setSenderAddressID(geliverConfiguration.getSenderAddressId());
        incomingRequest.setReturnAddressID(geliverConfiguration.getReturnAddressId());
        incomingRequest.setDistanceUnit("cm");
        incomingRequest.setMassUnit("kg");

        return geliverWebClient.post()
                .uri("/shipments")
                .bodyValue(incomingRequest)
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new ShipmentException("Geliver API Error: " + error))))
                .bodyToMono(GeliverMainResponse.class)
                .doOnNext(res -> log.info("Shipment created successfully. Shipment ID: {}",
                        res.getData() != null ? res.getData().getId() : "N/A"))
                .block();
    }

    @Override
    public GeliverTransactionMainResponse offerPurchase(GeliverTransactionCreateRequest transactionRequest) {
        log.info("Purchasing offer for provider service: {}", transactionRequest.getProviderServiceCode());

        transactionRequest.getShipment().setTest(Boolean.parseBoolean(geliverConfiguration.getTestMode()));

        return geliverWebClient.post()
                .uri("/transactions")
                .bodyValue(transactionRequest)
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> response.bodyToMono(String.class).flatMap(
                                error -> Mono.error(new ShipmentException("Purchase Transaction Failed: " + error))))
                .bodyToMono(GeliverTransactionMainResponse.class)
                .doOnNext(res -> log.info("Transaction completed. Barcode: {}",
                        res.getData().getShipment().getBarcode()))
                .block();
    }

    @Override
    public GeliverMainResponse listShipment(String orderNumber) {
        log.info("Listing shipments for order number: {}", orderNumber);
        try {
            return geliverWebClient.get()
                    .uri(uri -> uri.path("/shipments").queryParam("orderNumber", orderNumber).build())
                    .retrieve()
                    .bodyToMono(GeliverMainResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error occurred while listing shipment for order {}: {}", orderNumber, e.getMessage());
            throw new ShipmentException("Could not list shipments for order: " + orderNumber);
        }
    }
}