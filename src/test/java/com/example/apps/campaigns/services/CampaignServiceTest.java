package com.example.apps.campaigns.services;

import com.example.apps.campaigns.dtos.CampaignDTO;
import com.example.apps.campaigns.dtos.CouponDTO;
import com.example.apps.campaigns.entities.Campaign;
import com.example.apps.campaigns.entities.Coupon;
import com.example.apps.campaigns.repositories.CampaignRepository;
import com.example.apps.campaigns.repositories.CouponRepository;
import com.example.apps.campaigns.repositories.CouponUsageRepository;
import com.example.apps.campaigns.services.impl.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignService campaignService;

    private Coupon testCoupon;
    private Campaign testCampaign;

    @BeforeEach
    void setUp() {
        // Setup test coupon
        testCoupon = Coupon.builder()
                .code("TEST2025")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .minOrderAmount(BigDecimal.valueOf(100))
                .maxDiscountAmount(BigDecimal.valueOf(50))
                .usageLimit(100)
                .usagePerUser(1)
                .usageCount(0)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();
        testCoupon.setId(1L);

        // Setup test campaign
        testCampaign = Campaign.builder()
                .name("Yılbaşı Kampanyası")
                .campaignType(Campaign.CampaignType.CART_DISCOUNT)
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15))
                .minOrderAmount(BigDecimal.valueOf(200))
                .maxDiscountAmount(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .priority(10)
                .build();
        testCampaign.setId(1L);
    }

    // ==================== Coupon CRUD Tests ====================

    @Test
    void testCreateCoupon_Success() {
        // Given
        CouponDTO dto = new CouponDTO();
        dto.setCode("NEWCODE");
        dto.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        dto.setDiscountValue(BigDecimal.valueOf(20));
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusDays(30));

        when(couponRepository.existsByCode("NEWCODE")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> {
            Coupon saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        CouponDTO result = campaignService.createCoupon(dto);

        // Then
        assertNotNull(result);
        assertEquals("NEWCODE", result.getCode());
        assertEquals(0, result.getUsageCount());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testCreateCoupon_DuplicateCode_ThrowsException() {
        // Given
        CouponDTO dto = new CouponDTO();
        dto.setCode("EXISTING");

        when(couponRepository.existsByCode("EXISTING")).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> campaignService.createCoupon(dto));
        verify(couponRepository, never()).save(any());
    }

    @Test
    void testGetCouponById_Success() {
        // Given
        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));

        // When
        CouponDTO result = campaignService.getCouponById(1L);

        // Then
        assertNotNull(result);
        assertEquals("TEST2025", result.getCode());
        assertEquals(BigDecimal.valueOf(10), result.getDiscountValue());
    }

    @Test
    void testGetCouponById_NotFound_ThrowsException() {
        // Given
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> campaignService.getCouponById(999L));
    }

    // ==================== Coupon Validation Tests ====================

    @Test
    void testValidateCoupon_Success() {
        // Given
        when(couponRepository.findByCodeAndActive("TEST2025", true))
                .thenReturn(Optional.of(testCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 1L)).thenReturn(0);

        // When
        CouponDTO result = campaignService.validateCoupon("TEST2025", 1L, BigDecimal.valueOf(150));

        // Then
        assertNotNull(result);
        assertEquals("TEST2025", result.getCode());
    }

    @Test
    void testValidateCoupon_NotFound_ThrowsException() {
        // Given
        when(couponRepository.findByCodeAndActive("INVALID", true))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> campaignService.validateCoupon("INVALID", 1L, BigDecimal.valueOf(100)));
        assertTrue(exception.getMessage().contains("bulunamadı"));
    }

    @Test
    void testValidateCoupon_NotStarted_ThrowsException() {
        // Given
        testCoupon.setStartDate(LocalDateTime.now().plusDays(1));
        when(couponRepository.findByCodeAndActive("TEST2025", true))
                .thenReturn(Optional.of(testCoupon));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> campaignService.validateCoupon("TEST2025", 1L, BigDecimal.valueOf(100)));
        assertTrue(exception.getMessage().contains("henüz geçerli değil"));
    }

    @Test
    void testValidateCoupon_Expired_ThrowsException() {
        // Given
        testCoupon.setEndDate(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCodeAndActive("TEST2025", true))
                .thenReturn(Optional.of(testCoupon));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> campaignService.validateCoupon("TEST2025", 1L, BigDecimal.valueOf(100)));
        assertTrue(exception.getMessage().contains("süresi dolmuş"));
    }

    @Test
    void testValidateCoupon_BelowMinAmount_ThrowsException() {
        // Given
        when(couponRepository.findByCodeAndActive("TEST2025", true))
                .thenReturn(Optional.of(testCoupon));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> campaignService.validateCoupon("TEST2025", 1L, BigDecimal.valueOf(50)));
        assertTrue(exception.getMessage().contains("Minimum sepet tutarı"));
    }

    @Test
    void testValidateCoupon_UsageLimitReached_ThrowsException() {
        // Given
        testCoupon.setUsageLimit(10);
        testCoupon.setUsageCount(10);
        when(couponRepository.findByCodeAndActive("TEST2025", true))
                .thenReturn(Optional.of(testCoupon));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> campaignService.validateCoupon("TEST2025", 1L, BigDecimal.valueOf(150)));
        assertTrue(exception.getMessage().contains("kullanım limiti dolmuş"));
    }

    @Test
    void testValidateCoupon_UserLimitReached_ThrowsException() {
        // Given
        when(couponRepository.findByCodeAndActive("TEST2025", true))
                .thenReturn(Optional.of(testCoupon));
        when(couponUsageRepository.countByCouponIdAndUserId(1L, 1L)).thenReturn(1);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> campaignService.validateCoupon("TEST2025", 1L, BigDecimal.valueOf(150)));
        assertTrue(exception.getMessage().contains("daha fazla kullanamazsınız"));
    }

    // ==================== Discount Calculation Tests ====================

    @Test
    void testCalculateCouponDiscount_Percentage() {
        // Given
        CouponDTO coupon = new CouponDTO();
        coupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(10));
        BigDecimal orderAmount = BigDecimal.valueOf(200);

        // When
        BigDecimal discount = campaignService.calculateCouponDiscount(coupon, orderAmount);

        // Then
        assertEquals(BigDecimal.valueOf(20.00).setScale(2), discount.setScale(2));
    }

    @Test
    void testCalculateCouponDiscount_FixedAmount() {
        // Given
        CouponDTO coupon = new CouponDTO();
        coupon.setDiscountType(Coupon.DiscountType.FIXED_AMOUNT);
        coupon.setDiscountValue(BigDecimal.valueOf(50));
        BigDecimal orderAmount = BigDecimal.valueOf(200);

        // When
        BigDecimal discount = campaignService.calculateCouponDiscount(coupon, orderAmount);

        // Then
        assertEquals(BigDecimal.valueOf(50), discount);
    }

    @Test
    void testCalculateCouponDiscount_WithMaxLimit() {
        // Given
        CouponDTO coupon = new CouponDTO();
        coupon.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(20));
        coupon.setMaxDiscountAmount(BigDecimal.valueOf(30));
        BigDecimal orderAmount = BigDecimal.valueOf(200); // 20% = 40 TL

        // When
        BigDecimal discount = campaignService.calculateCouponDiscount(coupon, orderAmount);

        // Then
        assertEquals(BigDecimal.valueOf(30), discount);
    }

    @Test
    void testCalculateCouponDiscount_FreeShipping() {
        // Given
        CouponDTO coupon = new CouponDTO();
        coupon.setDiscountType(Coupon.DiscountType.FREE_SHIPPING);
        BigDecimal orderAmount = BigDecimal.valueOf(200);

        // When
        BigDecimal discount = campaignService.calculateCouponDiscount(coupon, orderAmount);

        // Then
        assertEquals(BigDecimal.ZERO, discount);
    }

    // ==================== Campaign Tests ====================

    @Test
    void testCreateCampaign_Success() {
        // Given
        CampaignDTO dto = new CampaignDTO();
        dto.setName("Test Campaign");
        dto.setCampaignType(Campaign.CampaignType.CART_DISCOUNT);
        dto.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        dto.setDiscountValue(BigDecimal.valueOf(15));
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusDays(30));

        when(campaignRepository.save(any(Campaign.class))).thenAnswer(i -> {
            Campaign saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        CampaignDTO result = campaignService.createCampaign(dto);

        // Then
        assertNotNull(result);
        assertEquals("Test Campaign", result.getName());
        verify(campaignRepository).save(any(Campaign.class));
    }

    @Test
    void testGetActiveCampaigns() {
        // Given
        when(campaignRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(testCampaign));

        // When
        List<CampaignDTO> result = campaignService.getActiveCampaigns();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Yılbaşı Kampanyası", result.get(0).getName());
    }

    @Test
    void testFindBestCampaign() {
        // Given
        Campaign campaign1 = Campaign.builder()
                .name("Campaign 1")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .minOrderAmount(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .priority(5)
                .build();
        campaign1.setId(1L);

        Campaign campaign2 = Campaign.builder()
                .name("Campaign 2")
                .discountType(Coupon.DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(20))
                .minOrderAmount(BigDecimal.valueOf(100))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .active(true)
                .priority(10)
                .build();
        campaign2.setId(2L);

        when(campaignRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(campaign2, campaign1));

        // When
        CampaignDTO result = campaignService.findBestCampaign(BigDecimal.valueOf(200), Arrays.asList(1L, 2L));

        // Then
        assertNotNull(result);
        assertEquals("Campaign 2", result.getName()); // Higher discount wins
    }

    @Test
    void testUpdateCoupon_Success() {
        // Given
        CouponDTO dto = new CouponDTO();
        dto.setCode("TEST2025");
        dto.setDiscountValue(BigDecimal.valueOf(15));
        dto.setDiscountType(Coupon.DiscountType.PERCENTAGE);
        dto.setStartDate(LocalDateTime.now());
        dto.setEndDate(LocalDateTime.now().plusDays(30));

        when(couponRepository.findById(1L)).thenReturn(Optional.of(testCoupon));
        when(couponRepository.save(any(Coupon.class))).thenAnswer(i -> i.getArgument(0));

        // When
        CouponDTO result = campaignService.updateCoupon(1L, dto);

        // Then
        assertNotNull(result);
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void testDeleteCoupon_Success() {
        // When
        campaignService.deleteCoupon(1L);

        // Then
        verify(couponRepository).deleteById(1L);
    }
}
