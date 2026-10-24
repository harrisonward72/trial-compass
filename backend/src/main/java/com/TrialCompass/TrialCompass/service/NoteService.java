package com.TrialCompass.TrialCompass.service;

import com.TrialCompass.TrialCompass.DTO.NoteRequest;
import com.TrialCompass.TrialCompass.DTO.NoteResponse;
import com.TrialCompass.TrialCompass.entity.Note;
import com.TrialCompass.TrialCompass.entity.SavedStudies;
import com.TrialCompass.TrialCompass.exception.ResourceNotFoundException;
import com.TrialCompass.TrialCompass.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//every method checks to make sure a saved trial belongs to the method caller
//each note is limited to that specific trial, there are no global notes

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final SavedStudyService savedStudyService;


    public NoteService(NoteRepository noteRepository, SavedStudyService savedStudyService) {
        this.noteRepository = noteRepository;
        this.savedStudyService = savedStudyService;
    }

    //CREATE - a new note
    @Transactional
    public NoteResponse create(String email, Long studyId, NoteRequest request) {
        SavedStudies study = savedStudyService.findOwned(email, studyId); //verifying saved study matches the caller
        Note note = new Note(study, request.body().trim());
        return toResponse(noteRepository.save(note));
    }

    //READ - all notes for one trial, beginning with newest note
    @Transactional(readOnly = true)
    public List<NoteResponse> list(String email, Long studyId) {
        SavedStudies study = savedStudyService.findOwned(email, studyId);
        return noteRepository.findBySavedStudiesIdOrderByCreatedAtDesc(study.getId()).stream().map(this::toResponse).toList();
    }

    //UPDATE
    @Transactional
    public NoteResponse update(String email, Long studyId, Long noteId, NoteRequest request) {
        Note note = findNote(email, studyId, noteId);
        note.setBody(request.body().trim());
        return toResponse(noteRepository.saveAndFlush(note)); //updates note, to include updatedAt time
    }

    //DELETE
    @Transactional
    public void delete(String email, Long studyId, Long noteId) {
        noteRepository.delete(findNote(email, studyId, noteId));
    }


    //helpers ==================================

    private Note findNote(String email, Long studyId, Long noteId) {
        SavedStudies study = savedStudyService.findOwned(email, studyId);
        return noteRepository.findByIdAndSavedStudiesId(noteId, study.getId()).orElseThrow(() -> new ResourceNotFoundException("Note not found"));
    }

    private NoteResponse toResponse(Note n) {
        return new NoteResponse(n.getId(), n.getBody(), n.getCreatedAt(), n.getUpdatedAt());
    }
}
