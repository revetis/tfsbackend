package com.example.apps.campaigns.services;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CouponDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ICampaignService {
    // Coupon operations
    List<CouponDTO> getAllCoupons();

    List<CouponDTO> getActiveCoupons();

    CouponDTO getCouponById(Long id);

    CouponDTO getCouponByCode(String code);

    CouponDTO createCoupon(CouponDTO dto);

    CouponDTO updateCoupon(Long id, CouponDTO dto);

    void deleteCoupon(Long id);

    // Coupon validation
    CouponDTO validateCoupon(String code, Long userId, BigDecimal orderAmount);

    BigDecimal calculateCouponDiscount(CouponDTO coupon, BigDecimal orderAmount);

    // Campaign operations
    List<CampaignDTO> getAllCampaigns();

    List<CampaignDTO> getActiveCampaigns();

    CampaignDTO getCampaignById(Long id);

    CampaignDTO createCampaign(CampaignDTO dto);

    CampaignDTO updateCampaign(Long id, CampaignDTO dto);

    void deleteCampaign(Long id);

    // Campaign application
    CampaignDTO findBestCampaign(BigDecimal orderAmount, List<Long> productIds);

    BigDecimal calculateCampaignDiscount(CampaignDTO campaign, BigDecimal orderAmount);
}
