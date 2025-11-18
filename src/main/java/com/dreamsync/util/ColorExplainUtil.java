package com.dreamsync.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorExplainUtil {

    // Returns list of top-k dominant colors as int[]{r,g,b}, most frequent first.
    // Simple quantization: each channel 0-15 (4 bits) -> 4096 buckets total (manageable).
    public static List<int[]> getDominantColors(File f, int k) throws Exception {
        BufferedImage img = ImageIO.read(f);
        if (img == null) throw new IllegalArgumentException("Invalid image: " + f.getAbsolutePath());

        int w = img.getWidth(), h = img.getHeight();
        Map<Integer, Integer> bucketCounts = new HashMap<>(); // key: packed bucket, value: count

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                // quantize to 4 bits per channel
                int rq = r >> 4;
                int gq = g >> 4;
                int bq = b >> 4;
                int key = (rq << 8) | (gq << 4) | bq;
                bucketCounts.put(key, bucketCounts.getOrDefault(key, 0) + 1);
            }
        }

        // sort buckets by frequency
        List<Map.Entry<Integer,Integer>> list = new ArrayList<>(bucketCounts.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer,Integer>>() {
            @Override
            public int compare(Map.Entry<Integer,Integer> a, Map.Entry<Integer,Integer> b) {
                return b.getValue() - a.getValue();
            }
        });

        List<int[]> result = new ArrayList<>();
        int added = 0;
        for (Map.Entry<Integer,Integer> e : list) {
            if (added >= k) break;
            int key = e.getKey();
            int rq = (key >> 8) & 0xF;
            int gq = (key >> 4) & 0xF;
            int bq = key & 0xF;
            // convert back to 0-255 approx by centering bucket
            int r = (rq * 16) + 8;
            int g = (gq * 16) + 8;
            int b = (bq * 16) + 8;
            result.add(new int[] { r, g, b });
            added++;
        }
        return result;
    }

    // Convert RGB to HSV (h in degrees 0..360, s,v 0..1)
    public static double[] rgbToHsv(int r, int g, int b) {
        double rd = r/255.0, gd = g/255.0, bd = b/255.0;
        double max = Math.max(rd, Math.max(gd, bd));
        double min = Math.min(rd, Math.min(gd, bd));
        double h=0, s=0, v=max;
        double d = max - min;
        s = max == 0 ? 0 : d / max;
        if (d == 0) {
            h = 0;
        } else {
            if (max == rd) {
                h = ((gd - bd) / d) % 6.0;
            } else if (max == gd) {
                h = ((bd - rd) / d) + 2.0;
            } else {
                h = ((rd - gd) / d) + 4.0;
            }
            h = h * 60.0;
            if (h < 0) h += 360.0;
        }
        return new double[] { h, s, v };
    }

    // Hue difference in degrees (0..180)
    public static double hueDistance(double h1, double h2) {
        double d = Math.abs(h1 - h2);
        return d > 180 ? 360 - d : d;
    }

    // quick color name from hue & brightness
    public static String approximateColorName(int[] rgb) {
        double[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
        double h = hsv[0], s = hsv[1], v = hsv[2];
        if (v < 0.15) return "black";
        if (v > 0.9 && s < 0.15) return "white";
        if (s < 0.25) return "gray";
        // hue ranges: red 0-15 & 345-360, orange 15-45, yellow 45-75, green 75-165, cyan 165-195,
        // blue 195-270, magenta 270-330
        if ( (h >= 345 && h <= 360) || (h >= 0 && h < 15)) return "red";
        if (h >= 15 && h < 45) return "orange";
        if (h >= 45 && h < 75) return "yellow";
        if (h >= 75 && h < 165) return "green";
        if (h >= 165 && h < 195) return "cyan";
        if (h >= 195 && h < 270) return "blue";
        if (h >= 270 && h < 330) return "magenta";
        return "brown";
    }

    // luminance (perceived brightness) 0..1
    public static double luminance(int[] rgb) {
        // ITU-R BT.709
        double r = rgb[0]/255.0, g = rgb[1]/255.0, b = rgb[2]/255.0;
        return 0.2126*r + 0.7152*g + 0.0722*b;
    }

    // Compare two images files and produce plain-English explanation
    public static List<String> explainPair(File fa, File fb, int hamming, double colorDist,
                                           int[] avgA, int[] avgB) {
        List<String> messages = new ArrayList<>();
        try {
            List<int[]> domA = getDominantColors(fa, 3);
            List<int[]> domB = getDominantColors(fb, 3);

            int[] aTop = domA.size() > 0 ? domA.get(0) : avgA;
            int[] bTop = domB.size() > 0 ? domB.get(0) : avgB;

            String aTopName = approximateColorName(aTop);
            String bTopName = approximateColorName(bTop);

            double[] hsvA = rgbToHsv(aTop[0], aTop[1], aTop[2]);
            double[] hsvB = rgbToHsv(bTop[0], bTop[1], bTop[2]);
            double hueDiff = hueDistance(hsvA[0], hsvB[0]);

            // background (top-1) comparison
            if (hueDiff > 25 || colorDist > 50) {
                messages.add(String.format("Background tone changed: sceneA dominant color appears %s, sceneB dominant color appears %s (hue diff=%.1f°, colorDist=%.1f).",
                        aTopName, bTopName, hueDiff, colorDist));
            } else {
                messages.add(String.format("Background tone is similar: both scenes dominant color around %s.", aTopName));
            }

            // second most dominant -> possibly clothing or subject color
            if (domA.size() > 1 && domB.size() > 1) {
                int[] a2 = domA.get(1);
                int[] b2 = domB.get(1);
                String a2Name = approximateColorName(a2);
                String b2Name = approximateColorName(b2);
                double[] hsvA2 = rgbToHsv(a2[0], a2[1], a2[2]);
                double[] hsvB2 = rgbToHsv(b2[0], b2[1], b2[2]);
                double hueDiff2 = hueDistance(hsvA2[0], hsvB2[0]);
                if (hueDiff2 > 25) {
                    messages.add(String.format("Secondary prominent color differs (likely clothing/prop): sceneA ~%s, sceneB ~%s (hue diff=%.1f°).",
                            a2Name, b2Name, hueDiff2));
                } else {
                    messages.add(String.format("Secondary prominent color is similar (~%s).", a2Name));
                }
            }

            // brightness/lighting
            double lumA = luminance(aTop);
            double lumB = luminance(bTop);
            double lumDiff = Math.abs(lumA - lumB);
            if (lumDiff > 0.15) {
                String brighter = lumA > lumB ? "sceneA is brighter" : "sceneB is brighter";
                messages.add(String.format("Lighting changed: %s (luminance diff=%.2f).", brighter, lumDiff));
            } else {
                messages.add("Lighting is similar between the scenes.");
            }

            // structure / appearance via hamming
            if (hamming > 12) {
                messages.add(String.format("Structural / appearance change detected (hamming=%d). This often means character pose/face/shape changed.", hamming));
            } else {
                messages.add(String.format("No major structural change detected (hamming=%d).", hamming));
            }

            // add explicit avg color information
            messages.add(String.format("Average colors — sceneA RGB(%d,%d,%d), sceneB RGB(%d,%d,%d).",
                    avgA[0], avgA[1], avgA[2], avgB[0], avgB[1], avgB[2]));

        } catch (Exception ex) {
            messages.add("Could not compute detailed explanation: " + ex.getMessage());
        }
        return messages;
    }
}
