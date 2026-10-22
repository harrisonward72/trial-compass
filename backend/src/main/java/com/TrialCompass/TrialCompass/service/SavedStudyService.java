package com.TrialCompass.TrialCompass.service;

import com.TrialCompass.TrialCompass.DTO.SaveStudyRequest;
import com.TrialCompass.TrialCompass.DTO.SavedStudyResponse;
import com.TrialCompass.TrialCompass.DTO.TrackingStatusResponse;
import com.TrialCompass.TrialCompass.DTO.UpdateSavedStudyRequest;
import com.TrialCompass.TrialCompass.entity.SavedStudies;
import com.TrialCompass.TrialCompass.entity.TrackingStatus;
import com.TrialCompass.TrialCompass.entity.User;
import com.TrialCompass.TrialCompass.exception.DuplicateSavedStudyException;
import com.TrialCompass.TrialCompass.exception.InvalidRequestException;
import com.TrialCompass.TrialCompass.exception.ResourceNotFoundException;
import com.TrialCompass.TrialCompass.repository.SavedStudyRepository;
import com.TrialCompass.TrialCompass.repository.TrackingStatusRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//business logic for saved trials - methods take the users email used frm the login token taken by the controller
//CRUD operations for saved trials

@Service
public class SavedStudyService {

    private static final String DEFAULT_TRACKING_STATUS = "INTERESTED";
    private final SavedStudyRepository savedStudyRepository;
    private final TrackingStatusRepository trackingStatusRepository;
    private final UserService userService;

    public SavedStudyService(SavedStudyRepository savedStudyRepository, TrackingStatusRepository trackingStatusRepository, UserService userService) {
        this.savedStudyRepository = savedStudyRepository;
        this.trackingStatusRepository = trackingStatusRepository;
        this.userService = userService;
    }

    //tracking status dropdown from mysql
    @Transactional(readOnly = true)
    public List<TrackingStatusResponse> getTrackingStatuses() {
        return trackingStatusRepository.findAllByOrderBySortOrderAsc().stream()
                .map(s -> new TrackingStatusResponse(s.getCode(), s.getLabel()))
                .toList();
    }

    //READ the users saved trials by newest first
    @Transactional(readOnly = true)
    public List<SavedStudyResponse> list(String email) {
        User user = userService.getByEmail(email);
        return savedStudyRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::toResponse).toList();
    }

    //SAVE a trial for the user
    //checks to see if the user is trying to save a duplicate trial - sql will not allow duplicate copies -- spring will throw an error
    @Transactional(readOnly = true)
    public SavedStudyResponse save(String email, SaveStudyRequest request) {
        User user = userService.getByEmail(email);

        if(savedStudyRepository.existsByUserIdAndNctId(user.getId(), request.nctId())) {
            throw new DuplicateSavedStudyException(request.nctId());
        }

        TrackingStatus status = findTrackingStatus(
                request.trackingStatusCode() != null && !request.trackingStatusCode().isBlank() ? request.trackingStatusCode().trim() : DEFAULT_TRACKING_STATUS
        );

        SavedStudies saved = new SavedStudies(
                user, status, request.nctId(), request.title().trim(), request.lastKnownStatus(), request.followUpDate()
        );

        try {
            return toResponse(savedStudyRepository.save(saved));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSavedStudyException(request.nctId());
        }
    }

    //UPDATE - changes tracking status and follow up date of a saved trial
    @Transactional
    public SavedStudyResponse update(String email, Long id, UpdateSavedStudyRequest request) {
        SavedStudies saved  = findOwned(email, id);

        saved.setTrackingStatus(findTrackingStatus(request.trackingStatusCode().trim()));
        saved.setFollowUpDate(request.followUpDate());

        //hibernate writes the update so the trial does not need to be saved/overwritten - just return the updated object
        return toResponse(saved);
    }

    //DELETE - a single saved trial
    @Transactional
    public void delete(String email, Long id) {
        savedStudyRepository.delete(findOwned(email, id));
    }


    //helper methods===========================================================================

    //finds a saved trial if it belongs to the user whos requesting it
    private SavedStudies findOwned(String email, Long id) {
        User user = userService.getByEmail(email);
        return savedStudyRepository.findByUserId(id, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Trial does not appear to be saved"));
    }

    private TrackingStatus findTrackingStatus(String code) {
        return trackingStatusRepository.findByCode(code).orElseThrow(() -> new InvalidRequestException("Unknown tracking status"));
    }

    private SavedStudyResponse toResponse(SavedStudies s) {
        return new SavedStudyResponse(
                s.getId(),
                s.getNctId(),
                s.getTitle(),
                s.getLastKnownStatus(),
                s.getTrackingStatus().getCode(),
                s.getTrackingStatus().getLabel(),
                s.getFollowUpDate(),
                s.getCreatedAt()
        );
    }

}
