package com.example.apps.campaigns.services.impl;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CampaignRequestItem;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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
    public CampaignDTO findBestCampaign(BigDecimal orderAmount, List<Long> productIds, List<Long> categoryIds) {
        List<CampaignDTO> activeCampaigns = getActiveCampaigns();

        CampaignDTO bestCampaign = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (CampaignDTO campaign : activeCampaigns) {
            // Check minimum order amount
            if (campaign.getMinOrderAmount() != null && orderAmount.compareTo(campaign.getMinOrderAmount()) < 0) {
                continue;
            }

            // Check if applicable to any of the products OR categories
            boolean applicable = false;
            // 1. Check cart discount
            if (campaign.getCampaignType() == Campaign.CampaignType.CART_DISCOUNT) {
                applicable = true;
            } else {
                // 2. Check each product one by one if it's product/category discount
                if (productIds != null) {
                    for (int i = 0; i < productIds.size(); i++) {
                        Long productId = productIds.get(i);
                        Long categoryId = (categoryIds != null && categoryIds.size() > i) ? categoryIds.get(i) : null;
                        if (isApplicable(campaign, List.of(productId), categoryId)) {
                            applicable = true;
                            break;
                        }
                    }
                }
            }

            if (!applicable) {
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
    public List<CampaignDTO> getCampaignsByProduct(Long productId, Long categoryId) {
        List<CampaignDTO> activeCampaigns = getActiveCampaigns();
        List<Long> pIds = productId != null ? List.of(productId) : List.of();

        return activeCampaigns.stream()
                .filter(c -> isApplicable(c, pIds, categoryId))
                .collect(Collectors.toList());
    }

    private boolean isApplicable(CampaignDTO campaign, List<Long> productIds, Long categoryId) {
        // If it's a CART_DISCOUNT, it applies to everything (usually with a min amount
        // check)
        if (campaign.getCampaignType() == Campaign.CampaignType.CART_DISCOUNT) {
            return true;
        }

        // Check product specific
        if (campaign.getCampaignType() == Campaign.CampaignType.PRODUCT_DISCOUNT && campaign.getProductIds() != null
                && !campaign.getProductIds().isBlank()) {
            List<Long> targetProductIds = java.util.Arrays.stream(campaign.getProductIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (productIds != null && productIds.stream().anyMatch(targetProductIds::contains)) {
                return true;
            }
        }

        // Check category specific
        if (campaign.getCampaignType() == Campaign.CampaignType.CATEGORY_DISCOUNT && campaign.getCategoryIds() != null
                && !campaign.getCategoryIds().isBlank()) {
            List<Long> targetCategoryIds = java.util.Arrays.stream(campaign.getCategoryIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (categoryId != null && targetCategoryIds.contains(categoryId)) {
                return true;
            }
        }

        // BUY_X_GET_Y usually matches products
        if (campaign.getCampaignType() == Campaign.CampaignType.BUY_X_GET_Y && campaign.getProductIds() != null
                && !campaign.getProductIds().isBlank()) {
            List<Long> targetProductIds = java.util.Arrays.stream(campaign.getProductIds().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (productIds != null && productIds.stream().anyMatch(targetProductIds::contains)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CampaignDTO findBestCampaign(BigDecimal orderAmount, List<CampaignRequestItem> items) {
        List<CampaignDTO> activeCampaigns = getActiveCampaigns();

        CampaignDTO bestCampaign = null;
        BigDecimal maxDiscount = BigDecimal.ZERO;

        for (CampaignDTO campaign : activeCampaigns) {
            BigDecimal discount = calculateCampaignDiscount(campaign, items);
            if (discount.compareTo(maxDiscount) > 0) {
                maxDiscount = discount;
                bestCampaign = campaign;
            }
        }

        return bestCampaign;
    }

    @Override
    public BigDecimal calculateCampaignDiscount(CampaignDTO campaign, List<CampaignRequestItem> items) {
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal orderAmount = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check global min order amount
        if (campaign.getMinOrderAmount() != null && orderAmount.compareTo(campaign.getMinOrderAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        switch (campaign.getCampaignType()) {
            case CART_DISCOUNT:
                // Sepet indirimi - Tutar üzerinden hesapla
                // Cart discount applies to total order amount
                discount = calculateCampaignDiscount(campaign, orderAmount);
                break;

            case PRODUCT_DISCOUNT:
            case CATEGORY_DISCOUNT:
                // Ürün/Kategori indirimi - Sadece ilgili ürünlerin toplamı üzerinden hesapla
                BigDecimal eligibleTotal = BigDecimal.ZERO;
                for (CampaignRequestItem item : items) {
                    if (isItemApplicable(campaign, item)) {
                        eligibleTotal = eligibleTotal
                                .add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                    }
                }

                if (eligibleTotal.compareTo(BigDecimal.ZERO) > 0) {
                    if (campaign.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
                        discount = eligibleTotal.multiply(campaign.getDiscountValue())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    } else if (campaign.getDiscountType() == Coupon.DiscountType.FIXED_AMOUNT) {
                        // Fixed amount applied once? Or per item?
                        // Usually "100 TL discount on Electronics category" implies once per cart for
                        // that category total.
                        discount = campaign.getDiscountValue();
                        // If discount > eligibleTotal, cap it later.
                    }
                }
                break;

            case BUY_X_GET_Y:
                // X Al Y Öde
                List<CampaignRequestItem> eligibleItems = items.stream()
                        .filter(i -> isItemApplicable(campaign, i))
                        .collect(Collectors.toList());

                if (!eligibleItems.isEmpty()) {
                    int totalQty = eligibleItems.stream().mapToInt(CampaignRequestItem::getQuantity).sum();
                    int X = campaign.getMinQuantity() != null ? campaign.getMinQuantity() : 0; // Buy X
                    int PayY = campaign.getDiscountValue() != null ? campaign.getDiscountValue().intValue() : 0; // Pay
                                                                                                                 // Y
                                                                                                                 // (Note:
                                                                                                                 // reusing
                                                                                                                 // discountValue)

                    if (X > 0 && totalQty >= X && PayY < X) {
                        // Logic: Buy X, Pay Y -> Free = X - PayY
                        // Applied for every X items group.
                        int freePerGroup = X - PayY;
                        int groups = totalQty / X;
                        int totalFreeCount = groups * freePerGroup;

                        if (totalFreeCount > 0) {
                            // Expand to unit prices to find cheapest ones
                            java.util.List<BigDecimal> allPrices = new java.util.ArrayList<>();
                            for (CampaignRequestItem item : eligibleItems) {
                                for (int k = 0; k < item.getQuantity(); k++) {
                                    allPrices.add(item.getPrice());
                                }
                            }
                            java.util.Collections.sort(allPrices); // Ascending order

                            for (int k = 0; k < totalFreeCount && k < allPrices.size(); k++) {
                                discount = discount.add(allPrices.get(k));
                            }
                        }
                    }
                }
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

    private boolean isItemApplicable(CampaignDTO campaign, CampaignRequestItem item) {
        // CART_DISCOUNT always true (if min amount passed)
        if (campaign.getCampaignType() == Campaign.CampaignType.CART_DISCOUNT) {
            return true;
        }

        // Check product Ids
        if (campaign.getProductIds() != null && !campaign.getProductIds().isBlank()) {
            java.util.List<String> targetProductIds = java.util.Arrays.asList(campaign.getProductIds().split(","));
            // Clean strings
            // Assuming strict Long match
            if (targetProductIds.stream().map(String::trim)
                    .anyMatch(s -> s.equals(String.valueOf(item.getProductId())))) {
                return true;
            }
        }

        // Check category Ids
        if (campaign.getCategoryIds() != null && !campaign.getCategoryIds().isBlank()) {
            java.util.List<String> targetCategoryIds = java.util.Arrays.asList(campaign.getCategoryIds().split(","));

            if (item.getCategoryId() != null && targetCategoryIds.stream().map(String::trim)
                    .anyMatch(s -> s.equals(String.valueOf(item.getCategoryId())))) {
                return true;
            }
            if (item.getMainCategoryId() != null && targetCategoryIds.stream().map(String::trim)
                    .anyMatch(s -> s.equals(String.valueOf(item.getMainCategoryId())))) {
                return true;
            }
        }

        // If neither product nor category filters are present, but type is PRODUCT or
        // CATEGORY discount,
        // does it apply to ALL? Usually no, it requires specific config.
        // But for safety:
        return false;
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

    // ==================== Paginated Operations ====================

    @Override
    public CouponPageResult getAllCoupons(int start, int end, String sortField, String sortOrder, String search) {
        int page = start / (end - start);
        int size = end - start;
        Sort.Direction direction = Sort.Direction.fromString(sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Specification<Coupon> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("code")), searchLike));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);
        List<CouponDTO> dtos = couponPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new CouponPageResult(dtos, couponPage.getTotalElements());
    }

    @Override
    public CampaignPageResult getAllCampaigns(int start, int end, String sortField, String sortOrder, String search) {
        int page = start / (end - start);
        int size = end - start;
        Sort.Direction direction = Sort.Direction.fromString(sortOrder);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Specification<Campaign> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String searchLike = "%" + search.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchLike));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Campaign> campaignPage = campaignRepository.findAll(spec, pageable);
        List<CampaignDTO> dtos = campaignPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new CampaignPageResult(dtos, campaignPage.getTotalElements());
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
