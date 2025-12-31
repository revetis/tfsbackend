package com.example.apps.campaigns.repositories;

import com.example.apps.campaigns.entities.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {
    List<CouponUsage> findByCouponId(Long couponId);

    int countByCouponIdAndUserId(Long couponId, Long userId);

    int countByCouponId(Long couponId);
}
