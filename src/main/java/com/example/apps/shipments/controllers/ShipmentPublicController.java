package com.example.apps.shipments.controllers;

import com.example.apps.shipments.dtos.GeliverShipmentCreateRequest;
import com.example.apps.shipments.dtos.GeliverTransactionCreateRequest;
import com.example.apps.shipments.services.IShipmentService;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest/api/public/shipments")
public class ShipmentPublicController {

    private final IShipmentService shipmentService;

    public ShipmentPublicController(IShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /** Efendim, ülkeler bazında asil şehir listesini getirir. */
    @GetMapping("/cities")
    public ResponseEntity<?> getAllCities(@RequestParam String countryCode) {
        var response = shipmentService.getCities(countryCode);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/cities",
                null, response));
    }

    /** Seçilen şehre ait ilçeleri o muazzam hızıyla listeler efendim. */
    @GetMapping("/districts")
    public ResponseEntity<?> getAllDistricts(@RequestParam String countryCode, @RequestParam String cityCode) {
        var response = shipmentService.getDistricts(countryCode, cityCode);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/districts",
                null, response));
    }

    /** Gönderi listeleme servisi efendim. */
    @GetMapping("/list")
    public ResponseEntity<?> listShipment(@RequestParam String orderNumber) {
        var response = shipmentService.listShipment(orderNumber);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/list",
                null, response));
    }

    /**
     * * Efendim, bu metod artık yeni GeliverShipmentCreateRequest DTO'su ile
     * teklifleri getirmek için kullanılacak.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createShipment(@RequestBody @Valid GeliverShipmentCreateRequest request) {
        var response = shipmentService.createShipment(request);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/create",
                null, response));
    }

    /**
     * * Efendim, burası en kritik nokta! Yeni 'Transaction' yapısına uygun olarak
     * hem provider kodunu hem de shipment verisini tek bir gövdede alıyoruz.
     */
    @PostMapping("/offer")
    public ResponseEntity<?> offerPurchase(@RequestBody @Valid GeliverTransactionCreateRequest request) {
        var response = shipmentService.offerPurchase(request);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, "/offer",
                null, response));
    }
}