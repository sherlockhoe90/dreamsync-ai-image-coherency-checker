package com.dreamsync.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="scene_id")
    private Scene scene;

    private String filename;
    private Integer width;
    private Integer height;
    private Long hash64;
    private Integer avgR;
    private Integer avgG;
    private Integer avgB;
    private LocalDateTime createdAt = LocalDateTime.now();
}