package com.example.apps.campaigns.repositories;

import com.example.apps.campaigns.entities.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {
    List<Campaign> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            LocalDateTime startDate, LocalDateTime endDate);

    List<Campaign> findByActiveOrderByPriorityDesc(Boolean active);
}
