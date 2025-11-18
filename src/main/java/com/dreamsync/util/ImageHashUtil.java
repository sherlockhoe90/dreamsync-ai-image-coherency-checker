package com.dreamsync.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageHashUtil {

    // Resize to 8x8 and compute aHash (64-bit)
    public static long averageHash(File imageFile) throws Exception {
        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) throw new IllegalArgumentException("Invalid image file");

        BufferedImage gray = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(img, 0, 0, 8, 8, null);
        g.dispose();

        int[] pixels = new int[64];
        int sum = 0;
        int idx = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int val = gray.getRGB(x, y) & 0xFF;
                pixels[idx++] = val;
                sum += val;
            }
        }
        int avg = sum / 64;
        long hash = 0L;
        for (int i = 0; i < 64; i++) {
            if (pixels[i] >= avg) {
                hash |= (1L << i);
            }
        }
        return hash;
    }

    public static int hammingDistance(long a, long b) {
        return Long.bitCount(a ^ b);
    }

    public static int[] averageRGB(File imageFile) throws Exception {
        BufferedImage img = ImageIO.read(imageFile);
        int w = img.getWidth(), h = img.getHeight();
        long r = 0, g = 0, b = 0;
        long total = (long) w * h;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                r += (rgb >> 16) & 0xFF;
                g += (rgb >> 8) & 0xFF;
                b += (rgb) & 0xFF;
            }
        }
        return new int[]{(int) (r / total), (int) (g / total), (int) (b / total)};
    }

    public static double colorDistance(int[] a, int[] b) {
        int dr = a[0] - b[0], dg = a[1] - b[1], db = a[2] - b[2];
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }
}
