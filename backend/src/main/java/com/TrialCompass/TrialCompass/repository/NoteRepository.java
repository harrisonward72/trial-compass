package com.TrialCompass.TrialCompass.repository;

import com.TrialCompass.TrialCompass.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findBySavedStudiesIdOrderByCreatedAtDesc(Long savedStudiesId);

    Optional<Note> findByIdAndSavedStudiesId(Long id, Long savedStudiesId);


    //uses sql to query the notes count for reports
    @Query(value = """
            SELECT n.saved_study_id AS savedStudyId, COUNT(*) AS total
            FROM notes n
            JOIN saved_studies s ON s.id = n.saved_study_id
            WHERE s.user_id = :userId
            GROUP BY n.saved_study_id
            """, nativeQuery = true)
    List<NoteCount> countNotesByStudyForUser(@Param("userId") Long userId);

    interface NoteCount {
        Long getSavedStudyId();
        Long getTotal();
    }
}
