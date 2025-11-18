package com.dreamsync.repository;

import com.dreamsync.model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByProjectId(Long projectId);
}
