package com.dreamsync.controller;

import com.dreamsync.model.ImageAsset;
import com.dreamsync.model.Project;
import com.dreamsync.model.Scene;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.SceneRepository;
import com.dreamsync.service.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class SceneController {
    private final ProjectRepository projectRepo;
    private final SceneRepository sceneRepo;
    private final ImageService imageService;

    public SceneController(ProjectRepository projectRepo, SceneRepository sceneRepo, ImageService imageService) {
        this.projectRepo = projectRepo;
        this.sceneRepo = sceneRepo;
        this.imageService = imageService;
    }

    @PostMapping("/projects/{pid}/scenes")
    public ResponseEntity<?> createScene(@PathVariable Long pid, @RequestBody Scene scene) {
        Project p = projectRepo.findById(pid).orElseThrow();
        scene.setProject(p);
        Scene saved = sceneRepo.save(scene);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/scenes/{sid}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long sid, @RequestPart("file") MultipartFile file) throws Exception {
        Scene scene = sceneRepo.findById(sid).orElseThrow();
        ImageAsset asset = imageService.saveImage(scene, file);
        return ResponseEntity.ok(asset);
    }
}