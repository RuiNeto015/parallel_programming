package filters;

import models.ChunkBoundaries;
import models.ConcurrentImage;

import java.awt.*;

public class GrayscaleFilter {
    public static void exec(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg, int contrast) {
        for (int i = boundaries.lowerBoundM(); i <= boundaries.upperBoundM(); i++) {
            for (int j = boundaries.lowerBoundN(); j <= boundaries.upperBoundN(); j++) {

                // fetches values of each pixel
                Color pixel = inputImg[i][j];
                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                int average = ((int) (r + g + b) / 3);

                Color newPixel = new Color(average, average, average);

                sharedImg.updatePixel(i, j, newPixel);
            }
        }
    }
}
