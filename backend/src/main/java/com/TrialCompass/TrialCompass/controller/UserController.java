package com.TrialCompass.TrialCompass.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.TrialCompass.TrialCompass.DTO.UserResponse;
import com.TrialCompass.TrialCompass.entity.User;
import com.TrialCompass.TrialCompass.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //pass in the auth object created
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        //return the email stored from the token
        //token identifies the user not the client - data cannot be stolen by changing a url
        User user = userService.getByEmail(authentication.getName());
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName());

    }
}
