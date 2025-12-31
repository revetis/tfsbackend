package com.example.apps.campaigns.controllers;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CouponDTO;
import com.example.apps.campaigns.services.ICampaignService;
import com.example.tfs.maindto.ApiTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/rest/api/public/campaigns")
@RequiredArgsConstructor
public class CampaignPublicController {

    private final ICampaignService campaignService;

    @PostMapping("/coupons/validate")
    public ResponseEntity<?> validateCoupon(
            @RequestParam String code,
            @RequestParam(required = false) Long userId,
            @RequestParam BigDecimal orderAmount) {

        try {
            CouponDTO coupon = campaignService.validateCoupon(code, userId, orderAmount);
            BigDecimal discount = campaignService.calculateCouponDiscount(coupon, orderAmount);

            return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                    true,
                    HttpStatus.SC_OK,
                    "/campaigns/coupons/validate",
                    null,
                    new CouponValidationResponse(coupon, discount)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiTemplate.apiTemplateGenerator(
                    false,
                    HttpStatus.SC_BAD_REQUEST,
                    "/campaigns/coupons/validate",
                    e.getMessage(),
                    null));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCampaigns() {
        List<CampaignDTO> campaigns = campaignService.getActiveCampaigns();
        return ResponseEntity.ok(ApiTemplate.apiTemplateGenerator(
                true,
                HttpStatus.SC_OK,
                "/campaigns/active",
                null,
                campaigns));
    }

    // Helper class for coupon validation response
    public static class CouponValidationResponse {
        public CouponDTO coupon;
        public BigDecimal discountAmount;

        public CouponValidationResponse(CouponDTO coupon, BigDecimal discountAmount) {
            this.coupon = coupon;
            this.discountAmount = discountAmount;
        }
    }
}
