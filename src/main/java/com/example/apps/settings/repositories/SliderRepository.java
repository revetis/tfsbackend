package com.example.apps.settings.repositories;

import com.example.apps.settings.entities.Slider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SliderRepository extends JpaRepository<Slider, Long> {
    List<Slider> findByActiveOrderByDisplayOrderAsc(Boolean active);

    List<Slider> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByDisplayOrderAsc(
            LocalDateTime startDate, LocalDateTime endDate);

    List<Slider> findAllByOrderByDisplayOrderAsc();
}
