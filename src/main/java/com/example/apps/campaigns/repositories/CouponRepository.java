package com.example.apps.campaigns.repositories;

import com.example.apps.campaigns.entities.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {
    Optional<Coupon> findByCodeAndActive(String code, Boolean active);

    List<Coupon> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDateTime startDate, LocalDateTime endDate);

    boolean existsByCode(String code);
}
