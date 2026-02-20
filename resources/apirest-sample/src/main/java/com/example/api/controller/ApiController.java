package com.example.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiController {

    @GetMapping("/api/status")
    public ResponseEntity<Map<String, String>> status() {
        return ResponseEntity
                .status(200)
                .body(Map.of("message", "API up and running."));
    }
}
