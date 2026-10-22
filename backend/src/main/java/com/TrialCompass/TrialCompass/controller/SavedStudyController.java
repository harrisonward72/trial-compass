package com.TrialCompass.TrialCompass.controller;

import com.TrialCompass.TrialCompass.DTO.SaveStudyRequest;
import com.TrialCompass.TrialCompass.DTO.SavedStudyResponse;
import com.TrialCompass.TrialCompass.DTO.TrackingStatusResponse;
import com.TrialCompass.TrialCompass.DTO.UpdateSavedStudyRequest;
import com.TrialCompass.TrialCompass.service.SavedStudyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//Everything under this URL path will require the user to be logged in - security config validates this
@RestController
@RequestMapping("/api/saved-studies")
public class SavedStudyController {

    private final SavedStudyService savedStudyService;

    public SavedStudyController(SavedStudyService savedStudyService) {
        this.savedStudyService = savedStudyService;
    }

    //GET dashboard list
    @GetMapping
    public List<SavedStudyResponse> list(Authentication authentication) {
        return savedStudyService.list(authentication.getName()); //return the users email address from the verified token
    }

    //tracking status dropdown options
    @GetMapping("/tracking-statuses")
    public List<TrackingStatusResponse> trackingStatuses() {
        return savedStudyService.getTrackingStatuses();
    }

    //POST - allows user to save a trial
    @PostMapping
    public ResponseEntity<SavedStudyResponse> save(@Valid @RequestBody SaveStudyRequest request, Authentication authentication) {
        SavedStudyResponse body = savedStudyService.save(authentication.getName(),request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    //PUT - change status and follow up date
    @PutMapping("/{id}")
    public SavedStudyResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSavedStudyRequest request, Authentication authentication) {
        return savedStudyService.update(authentication.getName(), id, request);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        savedStudyService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
