package com.TrialCompass.TrialCompass.repository;

import com.TrialCompass.TrialCompass.entity.SavedStudies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedStudyRepository extends JpaRepository<SavedStudies, Long> {

    List<SavedStudies> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<SavedStudies> findByUserId(Long id, Long userId);

    boolean existsByUserIdAndNctId(Long userId, String nctId);

    Optional<SavedStudies> findByIdAndUserId(Long id, Long userId);
}
