package com.dreamsync.repository;

import com.dreamsync.model.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {
    List<ImageAsset> findBySceneId(Long sceneId);
}