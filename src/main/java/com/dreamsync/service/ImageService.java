package com.dreamsync.service;

import com.dreamsync.model.ImageAsset;
import com.dreamsync.model.Scene;
import com.dreamsync.repository.ImageAssetRepository;
import com.dreamsync.util.ImageHashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ImageService {

    private final ImageAssetRepository imageAssetRepository;

    @Value("${dreamsync.uploads-dir}")
    private String uploadsDir;

    public ImageService(ImageAssetRepository imageAssetRepository) {
        this.imageAssetRepository = imageAssetRepository;
    }

    public ImageAsset saveImage(Scene scene, MultipartFile file) throws Exception {
        Path projectDir = Path.of(uploadsDir, String.valueOf(scene.getProject().getId()), String.valueOf(scene.getId()));
        Files.createDirectories(projectDir);
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path dest = projectDir.resolve(filename);
        try (var in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        File disk = dest.toFile();
        long hash = ImageHashUtil.averageHash(disk);
        int[] avg = ImageHashUtil.averageRGB(disk);
        BufferedImage bi = ImageIO.read(disk);
        ImageAsset asset = new ImageAsset();
        asset.setScene(scene);
        asset.setFilename(dest.toString());
        asset.setHash64(hash);
        asset.setAvgR(avg[0]); asset.setAvgG(avg[1]); asset.setAvgB(avg[2]);
        asset.setWidth(bi.getWidth()); asset.setHeight(bi.getHeight());
        return imageAssetRepository.save(asset);
    }
}