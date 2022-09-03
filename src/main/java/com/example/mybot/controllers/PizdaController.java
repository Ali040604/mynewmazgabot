package com.example.mybot.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PizdaController {
    @GetMapping("/")
    public String welcome() {
        return "Welcome to my gavno app";
    }
}
