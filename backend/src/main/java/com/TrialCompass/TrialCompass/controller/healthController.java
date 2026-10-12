package com.TrialCompass.TrialCompass.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class healthController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "ok",
                "message", "Trial Compass API is running",
                "timestamp", Instant.now().toString()
        );
    }
}
