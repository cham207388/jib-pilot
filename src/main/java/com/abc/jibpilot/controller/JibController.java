package com.abc.jibpilot.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jib")
public class JibController {

    @RequestMapping("/status")
    public String getStatus() {
        return "Jib Pilot is running!";
    }
}
