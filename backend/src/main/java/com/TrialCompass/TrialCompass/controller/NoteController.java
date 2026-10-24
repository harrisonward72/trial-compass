package com.TrialCompass.TrialCompass.controller;

import com.TrialCompass.TrialCompass.DTO.NoteRequest;
import com.TrialCompass.TrialCompass.DTO.NoteResponse;
import com.TrialCompass.TrialCompass.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/saved-studies/{studyId}/notes") //notes live within the path of a saved trial
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> list(@PathVariable Long studyId, Authentication authentication) {
        return noteService.list(authentication.getName(), studyId);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@PathVariable Long studyId, @Valid @RequestBody NoteRequest request, Authentication authentication) {
        NoteResponse body = noteService.create(authentication.getName(), studyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PutMapping("/{notedId}")
    public NoteResponse update(@PathVariable Long studyId, @PathVariable Long notedId, @Valid @RequestBody NoteRequest request, Authentication authentication) {
        return noteService.update(authentication.getName(), studyId, notedId, request);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<NoteResponse> delete(@PathVariable Long studyId, @PathVariable Long noteId, Authentication authentication) {
        noteService.delete(authentication.getName(), studyId, noteId);
        return ResponseEntity.noContent().build();
    }
}
