package com.example.api.controller;

import com.example.api.repository.UserRepository;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final UserRepository userRepository;

    @Autowired
    public WebController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        Map<String, String> db = new HashMap<>();
        try {
            long userCount = userRepository.count();
            db.put("status", "ok");
            db.put("message", "User count: " + userCount);
        } catch (DataAccessException ex) {
            db.put("status", "error");
            db.put("message", "Database connection failed: " + ex.getMessage());
        }
        model.addAttribute("db", db);

        return "home";
    }
}
