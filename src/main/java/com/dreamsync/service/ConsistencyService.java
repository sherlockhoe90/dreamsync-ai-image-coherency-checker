package com.dreamsync.service;

import com.dreamsync.model.ImageAsset;
import com.dreamsync.model.Scene;
import com.dreamsync.repository.ImageAssetRepository;
import com.dreamsync.repository.ProjectRepository;
import com.dreamsync.repository.SceneRepository;
import com.dreamsync.util.ColorExplainUtil;
import com.dreamsync.util.ImageHashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConsistencyService {

    private final SceneRepository sceneRepository;
    private final ImageAssetRepository imageAssetRepository;
    private final ProjectRepository projectRepository;

    @Value("${dreamsync.thresholds.hamming-similar}")
    private int HAMMING_THRESHOLD;

    @Value("${dreamsync.thresholds.color-dist}")
    private double COLOR_THRESHOLD;

    @Value("${dreamsync.thresholds.text-levenshtein-threshold}")
    private int TEXT_LEV_THRESH;

    public ConsistencyService(SceneRepository sceneRepository,
                              ImageAssetRepository imageAssetRepository,
                              ProjectRepository projectRepository) {
        this.sceneRepository = sceneRepository;
        this.imageAssetRepository = imageAssetRepository;
        this.projectRepository = projectRepository;
    }

    public Map<String, Object> analyzeProject(Long projectId) {
        Map<String, Object> result = new HashMap<>();
        List<Scene> scenes = sceneRepository.findByProjectId(projectId);

        // Gather assets per scene (pick first asset per scene for MVP)
        Map<Long, ImageAsset> sceneAsset = new HashMap<>();
        for (Scene s : scenes) {
            List<ImageAsset> assets = imageAssetRepository.findBySceneId(s.getId());
            if (assets != null && !assets.isEmpty()) {
                sceneAsset.put(s.getId(), assets.get(0));
            }
        }

        // Image pairwise comparison + human-readable explanations
        List<Map<String, Object>> imagePairs = new ArrayList<>();
        List<Long> sceneIds = new ArrayList<>(sceneAsset.keySet());
        for (int i = 0; i < sceneIds.size(); i++) {
            for (int j = i + 1; j < sceneIds.size(); j++) {
                Long aId = sceneIds.get(i), bId = sceneIds.get(j);
                ImageAsset a = sceneAsset.get(aId), b = sceneAsset.get(bId);
                if (a == null || b == null) continue;

                int hamming = (int) ImageHashUtil.hammingDistance(
                        a.getHash64() == null ? 0L : a.getHash64(),
                        b.getHash64() == null ? 0L : b.getHash64()
                );

                double colorDist = ImageHashUtil.colorDistance(
                        new int[] { safeInt(a.getAvgR()), safeInt(a.getAvgG()), safeInt(a.getAvgB()) },
                        new int[] { safeInt(b.getAvgR()), safeInt(b.getAvgG()), safeInt(b.getAvgB()) }
                );

                boolean similar = hamming <= HAMMING_THRESHOLD && colorDist <= COLOR_THRESHOLD;

                Map<String, Object> pair = new HashMap<>();
                pair.put("sceneA", a.getScene().getId());
                pair.put("sceneB", b.getScene().getId());
                pair.put("hamming", hamming);
                pair.put("colorDistance", colorDist);
                pair.put("isSimilar", similar);

                // Build human readable explanation using ColorExplainUtil
                try {
                    File fa = new File(a.getFilename());
                    File fb = new File(b.getFilename());
                    int[] avgA = new int[] { safeInt(a.getAvgR()), safeInt(a.getAvgG()), safeInt(a.getAvgB()) };
                    int[] avgB = new int[] { safeInt(b.getAvgR()), safeInt(b.getAvgG()), safeInt(b.getAvgB()) };

                    List<String> explanationLines = ColorExplainUtil.explainPair(fa, fb, hamming, colorDist, avgA, avgB);
                    pair.put("explanationLines", explanationLines);
                    pair.put("explanationSummary", explanationLines == null || explanationLines.isEmpty()
                            ? "No detailed explanation available."
                            : String.join(" ", explanationLines));
                } catch (Exception ex) {
                    pair.put("explanationError", ex.getMessage());
                }

                imagePairs.add(pair);
            }
        }

        // Text consistency: basic token extraction (capitalized tokens)
        Map<String, Set<Long>> tokenMap = new HashMap<>();
        for (Scene s : scenes) {
            if (s.getDescription() == null) continue;
            String[] tokens = s.getDescription().split("\\W+");
            for (String t : tokens) {
                if (t.length() > 2 && Character.isUpperCase(t.charAt(0))) {
                    String key = t.toLowerCase();
                    Set<Long> set = tokenMap.get(key);
                    if (set == null) {
                        set = new HashSet<>();
                        tokenMap.put(key, set);
                    }
                    set.add(s.getId());
                }
            }
        }
        // Find tokens that appear inconsistently
        List<Map<String, Object>> textIssues = new ArrayList<>();
        for (Map.Entry<String, Set<Long>> entry : tokenMap.entrySet()) {
            if (entry.getValue().size() < scenes.size()) {
                Map<String, Object> m = new HashMap<>();
                m.put("token", entry.getKey());
                m.put("appearsInScenes", entry.getValue());
                textIssues.add(m);
            }
        }

        result.put("imagePairs", imagePairs);
        result.put("textIssues", textIssues);
        result.put("projectId", projectId);
        result.put("sceneCount", scenes.size());
        return result;
    }

    // helper to avoid NPEs for Integer fields
    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }









    /*old*/
    /*
    public Map<String, Object> analyzeProject(Long projectId) {
        Map<String,Object> result = new HashMap<>();
        List<Scene> scenes = sceneRepository.findByProjectId(projectId);

        // Gather assets per scene (pick first asset per scene for MVP)
        Map<Long, ImageAsset> sceneAsset = new HashMap<>();
        for (Scene s : scenes) {
            var assets = imageAssetRepository.findBySceneId(s.getId());
            if (!assets.isEmpty()) sceneAsset.put(s.getId(), assets.get(0));
        }

        // Image pairwise comparison
        List<Map<String,Object>> imagePairs = new ArrayList<>();
        List<Long> sceneIds = new ArrayList<>(sceneAsset.keySet());
        for (int i=0;i<sceneIds.size();i++){
            for (int j=i+1;j<sceneIds.size();j++){
                Long aId = sceneIds.get(i), bId = sceneIds.get(j);
                ImageAsset a = sceneAsset.get(aId), b = sceneAsset.get(bId);
                if (a==null || b==null) continue;
                int hamming = (int) ImageHashUtil.hammingDistance(a.getHash64(), b.getHash64());
                double colorDist = ImageHashUtil.colorDistance(
                        new int[]{a.getAvgR(), a.getAvgG(), a.getAvgB()},
                        new int[]{b.getAvgR(), b.getAvgG(), b.getAvgB()}
                );
                boolean similar = hamming <= HAMMING_THRESHOLD && colorDist <= COLOR_THRESHOLD;
                Map<String,Object> pair = new HashMap<>();
                pair.put("sceneA", a.getScene().getId());
                pair.put("sceneB", b.getScene().getId());
                pair.put("hamming", hamming);
                pair.put("colorDistance", colorDist);
                pair.put("isSimilar", similar);
                imagePairs.add(pair);
            }
        }

        // Text consistency: basic token extraction (capitalized tokens)
        Map<String, Set<Long>> tokenMap = new HashMap<>();
        for (Scene s : scenes) {
            if (s.getDescription() == null) continue;
            String[] tokens = s.getDescription().split("\\W+");
            for (String t : tokens) {
                if (t.length() > 2 && Character.isUpperCase(t.charAt(0))) {
                    tokenMap.computeIfAbsent(t.toLowerCase(), k->new HashSet<>()).add(s.getId());
                }
            }
        }
        // Find tokens that appear inconsistently
        List<Map<String,Object>> textIssues = new ArrayList<>();
        for (var entry : tokenMap.entrySet()) {
            if (entry.getValue().size() < scenes.size()) {
                Map<String,Object> m = new HashMap<>();
                m.put("token", entry.getKey());
                m.put("appearsInScenes", entry.getValue());
                textIssues.add(m);
            }
        }

        result.put("imagePairs", imagePairs);
        result.put("textIssues", textIssues);
        result.put("projectId", projectId);
        return result;
    }
*/
}