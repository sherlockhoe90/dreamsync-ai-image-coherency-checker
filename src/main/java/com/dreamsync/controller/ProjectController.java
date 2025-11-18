package com.dreamsync.controller;

import com.dreamsync.model.Project;
import com.dreamsync.repository.ProjectRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository repo;

    public ProjectController(ProjectRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Project create(@RequestBody Project p) {
        return repo.save(p);
    }
}
