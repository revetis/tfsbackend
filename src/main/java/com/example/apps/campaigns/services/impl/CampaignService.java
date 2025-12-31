package com.example.apps.campaigns.services.impl;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CouponDTO;
import com.example.apps.campaigns.entities.Campaign;
import com.example.apps.campaigns.entities.Coupon;
import com.example.apps.campaigns.entities.CouponUsage;
import com.example.apps.campaigns.repositories.CampaignRepository;
import com.example.apps.campaigns.repositories.CouponRepository;
import com.example.apps.campaigns.repositories.CouponUsageRepository;
import com.example.apps.campaigns.services.ICampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignService implements ICampaignService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final CampaignRepository campaignRepository;

    // ==================== Coupon CRUD ====================

    @Override
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponDTO> getActiveCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));
        return convertToDTO(coupon);
    }

    @Override
    public CouponDTO getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCodeAndActive(code, true)
                .orElseThrow(() -> new RuntimeException("Coupon not found with code: " + code));
        return convertToDTO(coupon);
    }

    @Override
    @Transactional
    public CouponDTO createCoupon(CouponDTO dto) {
        if (couponRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Coupon with code '" + dto.getCode() + "' already exists");
        }

        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon, "id", "createdAt", "updatedAt", "usageCount");

        if (coupon.getActive() == null) {
            coupon.setActive(true);
        }
        if (coupon.getUsagePerUser() == null) {
            coupon.setUsagePerUser(1);
        }
        coupon.setUsageCount(0);

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created: {}", saved.getCode());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public CouponDTO updateCoupon(Long id, CouponDTO dto) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        // Check code uniqueness if changed
        if (!coupon.getCode().equals(dto.getCode()) && couponRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("Coupon with code '" + dto.getCode() + "' already exists");
        }

        BeanUtils.copyProperties(dto, coupon, "id", "createdAt", "updatedAt", "usageCount");
        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon updated: {}", saved.getCode());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
        log.info("Coupon deleted with id: {}", id);
    }

    // ==================== Coupon Validation ====================

    @Override
    public CouponDTO validateCoupon(String code, Long userId, BigDecimal orderAmount) {
        Coupon coupon = couponRepository.findByCodeAndActive(code, true)
                .orElseThrow(() -> new RuntimeException("Kupon bulunamadı veya aktif değil"));

        LocalDateTime now = LocalDateTime.now();

        // Check date validity
        if (coupon.getStartDate().isAfter(now)) {
            throw new RuntimeException("Kupon henüz geçerli değil");
        }
        if (coupon.getEndDate().isBefore(now)) {
            throw new RuntimeException("Kuponun geçerlilik süresi dolmuş");
        }

        // Check minimum order amount
        if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new RuntimeException("Minimum sepet tutarı: " + coupon.getMinOrderAmount() + " TL");
        }

        // Check total usage limit
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Kupon kullanım limiti dolmuş");
        }

        // Check user usage limit
        if (userId != null && coupon.getUsagePerUser() != null) {
            int userUsageCount = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsageCount >= coupon.getUsagePerUser()) {
                throw new RuntimeException("Bu kuponu daha fazla kullanamazsınız");
            }
        }

        return convertToDTO(coupon);
    }

    @Override
    public BigDecimal calculateCouponDiscount(CouponDTO coupon, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;

        switch (coupon.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount.multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discount = coupon.getDiscountValue();
                break;
            case FREE_SHIPPING:
                // Free shipping handled separately
                discount = BigDecimal.ZERO;
                break;
        }

        // Apply max discount limit
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        // Discount cannot exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    // ==================== Campaign CRUD ====================

    @Override
    public List<CampaignDTO> getAllCampaigns() {
        return campaignRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampaignDTO> getActiveCampaigns() {
        LocalDateTime now = LocalDateTime.now();
        return campaignRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(now, now)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CampaignDTO getCampaignById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));
        return convertToDTO(campaign);
    }

    @Override
    @Transactional
    public CampaignDTO createCampaign(CampaignDTO dto) {
        Campaign campaign = new Campaign();
        BeanUtils.copyProperties(dto, campaign, "id", "createdAt", "updatedAt");

        if (campaign.getActive() == null) {
            campaign.setActive(true);
        }
        if (campaign.getPriority() == null) {
            campaign.setPriority(0);
        }

        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign created: {}", saved.getName());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public CampaignDTO updateCampaign(Long id, CampaignDTO dto) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaign not found with id: " + id));

        BeanUtils.copyProperties(dto, campaign, "id", "createdAt", "updatedAt");
        Campaign saved = campaignRepository.save(campaign);
        log.info("Campaign updated: {}", saved.getName());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long id) {
        campaignRepository.deleteById(id);
        log.info("Campaign deleted with id: {}", id);
    }

    // ==================== Campaign Application ====================

    @Override
    public CampaignDTO findBestCampaign(BigDecimal orderAmount, List<Long> productIds) {
        List<CampaignDTO> activeCampaigns = getActiveCampaigns();

        CampaignDTO bestCampaign = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (CampaignDTO campaign : activeCampaigns) {
            // Check minimum order amount
            if (campaign.getMinOrderAmount() != null && orderAmount.compareTo(campaign.getMinOrderAmount()) < 0) {
                continue;
            }

            BigDecimal discount = calculateCampaignDiscount(campaign, orderAmount);
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                bestCampaign = campaign;
            }
        }

        return bestCampaign;
    }

    @Override
    public BigDecimal calculateCampaignDiscount(CampaignDTO campaign, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;

        switch (campaign.getDiscountType()) {
            case PERCENTAGE:
                discount = orderAmount.multiply(campaign.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discount = campaign.getDiscountValue();
                break;
            case FREE_SHIPPING:
                discount = BigDecimal.ZERO;
                break;
        }

        // Apply max discount limit
        if (campaign.getMaxDiscountAmount() != null && discount.compareTo(campaign.getMaxDiscountAmount()) > 0) {
            discount = campaign.getMaxDiscountAmount();
        }

        // Discount cannot exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    // ==================== Converters ====================

    private CouponDTO convertToDTO(Coupon entity) {
        CouponDTO dto = new CouponDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    private CampaignDTO convertToDTO(Campaign entity) {
        CampaignDTO dto = new CampaignDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
