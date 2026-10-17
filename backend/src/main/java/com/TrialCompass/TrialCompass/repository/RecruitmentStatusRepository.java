package com.TrialCompass.TrialCompass.repository;

import com.TrialCompass.TrialCompass.entity.RecruitmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

//dto object for searching for status'
public interface RecruitmentStatusRepository  extends JpaRepository<RecruitmentStatus, Long> {

    List<RecruitmentStatus> findAllByOrderBySortOrderAsc();

    List<RecruitmentStatus> findByCodeIn(Collection<String> codes);
}
