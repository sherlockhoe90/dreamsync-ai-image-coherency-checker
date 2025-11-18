package com.dreamsync.controller;

import com.dreamsync.service.ConsistencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ConsistencyController {
    private final ConsistencyService service;

    public ConsistencyController(ConsistencyService service) {
        this.service = service;
    }

    @GetMapping("/{pid}/consistency")
    public Object analyze(@PathVariable Long pid) {
        return service.analyzeProject(pid);
    }
}