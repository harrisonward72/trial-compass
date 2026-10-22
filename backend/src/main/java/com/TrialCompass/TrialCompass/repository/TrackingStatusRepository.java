package com.TrialCompass.TrialCompass.repository;

import com.TrialCompass.TrialCompass.entity.TrackingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackingStatusRepository extends JpaRepository<TrackingStatus, Long> {

    List<TrackingStatus> findAllByOrderBySortOrderAsc();

    Optional<TrackingStatus> findByCode(String code);
}
