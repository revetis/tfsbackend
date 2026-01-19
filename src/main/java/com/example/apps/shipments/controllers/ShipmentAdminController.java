package com.example.apps.shipments.controllers;

import com.example.apps.shipments.dtos.ShipmentDTO;
import com.example.apps.shipments.dtos.ShipmentEventDTO;
import com.example.apps.shipments.services.IShipmentService;
import com.example.tfs.maindto.ApiErrorTemplate;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import javax.resource.NotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/api/admin/shipments")
public class ShipmentAdminController {

        @Autowired
        private IShipmentService shipmentService;

        @GetMapping("/all")
        public ResponseEntity<ApiTemplate<Void, List<ShipmentDTO>>> getAllShipments(
                        @RequestParam(name = "page", defaultValue = "0") int page,
                        @RequestParam(name = "size", defaultValue = "10") int size,
                        @RequestParam(name = "sort", defaultValue = "createdAt") String sortField,
                        @RequestParam(name = "direction", defaultValue = "DESC") String sortOrder,
                        @RequestParam(name = "q", required = false) String search,
                        @RequestParam(name = "status", required = false) String status,
                        @RequestParam(name = "userId", required = false) Long userId,
                        HttpServletRequest request) {

                var result = shipmentService.getAllShipments(page, size, sortField, sortOrder, search, status, userId);

                return ResponseEntity.ok()
                                .header("X-Total-Count", String.valueOf(result.totalCount()))
                                .header("Access-Control-Expose-Headers", "X-Total-Count")
                                .body(ApiTemplate.<Void, List<ShipmentDTO>>apiTemplateGenerator(
                                                true,
                                                200,
                                                request.getRequestURI(),
                                                null,
                                                result.data()));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiTemplate<Void, ShipmentDTO>> getShipmentById(@PathVariable Long id,
                        HttpServletRequest request) {
                ShipmentDTO response = shipmentService.getShipmentById(id);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, request.getRequestURI(), null, response));
        }

        @PostMapping("/cancel/by-order-number")
        public ResponseEntity<?> cancelShipmentByOrderNumber(@RequestParam String orderNumber,
                        HttpServletRequest request) {
                try {
                        var response = shipmentService.cancelShipment(orderNumber);
                        return ResponseEntity.ok(
                                        ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                                                        request.getRequestURI(), null,
                                                        response));
                } catch (NotSupportedException e) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiTemplate.apiTemplateGenerator(
                                        false,
                                        HttpStatus.BAD_REQUEST.value(),
                                        request.getRequestURI(),
                                        ApiErrorTemplate.apiErrorTemplateGenerator(false,
                                                        HttpStatus.BAD_REQUEST.value(),
                                                        request.getRequestURI(), e.getMessage()),
                                        null));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiTemplate.apiTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        request.getRequestURI(),
                                                        ApiErrorTemplate.apiErrorTemplateGenerator(false,
                                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                                        request.getRequestURI(), e.getMessage()),
                                                        null));
                }
        }

        @PostMapping("/cancel/by-geliver-id")
        public ResponseEntity<?> cancelShipmentByGeliverId(@RequestParam String shipmentID,
                        HttpServletRequest request) {
                try {
                        var response = shipmentService.cancelShipmentByID(shipmentID);
                        return ResponseEntity.ok(
                                        ApiTemplate.apiTemplateGenerator(true, HttpStatus.OK.value(),
                                                        request.getRequestURI(), null,
                                                        response));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(ApiTemplate.apiTemplateGenerator(
                                                        false,
                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                        request.getRequestURI(),
                                                        ApiErrorTemplate.apiErrorTemplateGenerator(false,
                                                                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                                        request.getRequestURI(), e.getMessage()),
                                                        null));
                }
        }

        @GetMapping("/{id}/events")
        public ResponseEntity<ApiTemplate<Void, List<ShipmentEventDTO>>> getShipmentEvents(@PathVariable Long id,
                        HttpServletRequest request) {
                List<ShipmentEventDTO> response = shipmentService.getShipmentEvents(id);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, request.getRequestURI(), null, response));
        }

        @GetMapping("/events/by-order-number")
        public ResponseEntity<ApiTemplate<Void, List<ShipmentEventDTO>>> getShipmentEventsByOrderNumber(
                        @RequestParam String orderNumber, HttpServletRequest request) {
                List<ShipmentEventDTO> response = shipmentService.getShipmentEventsByOrderNumber(orderNumber);
                return ResponseEntity.ok(
                                ApiTemplate.apiTemplateGenerator(true, 200, request.getRequestURI(), null, response));
        }

        @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
        public ResponseEntity<?> deleteShipment(@PathVariable Long id, HttpServletRequest request) {
                shipmentService.deleteShipmentById(id);
                return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(true, 200, request.getRequestURI(), null,
                                "Shipment deleted successfully"));
        }
}
