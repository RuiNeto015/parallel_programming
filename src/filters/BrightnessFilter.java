package filters;

import models.ChunkBoundaries;
import models.ConcurrentImage;

import java.awt.*;

public class BrightnessFilter {
    public static void exec(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg, int brightnessAmount) {
        for (int i = boundaries.lowerBoundM(); i <= boundaries.upperBoundM(); i++) {
            for (int j = boundaries.lowerBoundN(); j <= boundaries.upperBoundN(); j++) {

                // fetches values of each pixel
                Color pixel = inputImg[i][j];
                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                // takes average of color values
                r = Math.min(r + brightnessAmount, 255);
                g = Math.min(g + brightnessAmount, 255);
                b = Math.min(b + brightnessAmount, 255);

                sharedImg.updatePixel(i, j, new Color(r, g, b));
            }
        }
    }
}
