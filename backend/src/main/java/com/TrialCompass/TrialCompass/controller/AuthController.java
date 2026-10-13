package com.TrialCompass.TrialCompass.controller;

import com.TrialCompass.TrialCompass.DTO.RegisterRequest;
import com.TrialCompass.TrialCompass.DTO.UserResponse;
import com.TrialCompass.TrialCompass.entity.User;
import com.TrialCompass.TrialCompass.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;


    public AuthController(UserService userService) {
        this.userService = userService;
    }

    //post mapping for user registration
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);

        //convert entity to a response DTO
        UserResponse body = new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName());

        //return response confirming registration
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
