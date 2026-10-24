package com.TrialCompass.TrialCompass.repository;

import com.TrialCompass.TrialCompass.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySavedStudiesIdOrderByCreatedAtDesc(Long savedStudiesId);

    Optional<Note> findByIdAndSavedStudiesId(Long id, Long savedStudiesId);
}
