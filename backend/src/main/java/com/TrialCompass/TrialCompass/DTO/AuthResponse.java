package com.TrialCompass.TrialCompass.DTO;

//what the client gets back after a valid login
//client will store the token and send it with later requests
public record AuthResponse(
        String token,
        String tokeType,
        Long expiresInSeconds, //knows when to log the user out
        UserResponse user //ui knows who is logging in
) {
}
