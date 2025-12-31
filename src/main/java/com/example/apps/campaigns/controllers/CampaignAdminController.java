package com.example.apps.campaigns.controllers;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CouponDTO;
import com.example.apps.campaigns.services.ICampaignService;
import com.example.tfs.maindto.ApiTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/api/admin/campaigns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CampaignAdminController {

    private final ICampaignService campaignService;

    // ==================== Coupons ====================

    @GetMapping("/coupons")
    public ResponseEntity<?> getAllCoupons() {
        List<CouponDTO> coupons = campaignService.getAllCoupons();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/coupons",
                null,
                coupons));
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable Long id) {
        CouponDTO coupon = campaignService.getCouponById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/coupons/" + id,
                null,
                coupon));
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody @Valid CouponDTO dto) {
        CouponDTO created = campaignService.createCoupon(dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/admin/campaigns/coupons",
                null,
                created));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @RequestBody @Valid CouponDTO dto) {
        CouponDTO updated = campaignService.updateCoupon(id, dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/coupons/" + id,
                null,
                updated));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        campaignService.deleteCoupon(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/coupons/" + id,
                null,
                true));
    }

    // ==================== Campaigns ====================

    @GetMapping
    public ResponseEntity<?> getAllCampaigns() {
        List<CampaignDTO> campaigns = campaignService.getAllCampaigns();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns",
                null,
                campaigns));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCampaignById(@PathVariable Long id) {
        CampaignDTO campaign = campaignService.getCampaignById(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/" + id,
                null,
                campaign));
    }

    @PostMapping
    public ResponseEntity<?> createCampaign(@RequestBody @Valid CampaignDTO dto) {
        CampaignDTO created = campaignService.createCampaign(dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_CREATED,
                "/admin/campaigns",
                null,
                created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCampaign(@PathVariable Long id, @RequestBody @Valid CampaignDTO dto) {
        CampaignDTO updated = campaignService.updateCampaign(id, dto);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/" + id,
                null,
                updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/admin/campaigns/" + id,
                null,
                true));
    }
}
