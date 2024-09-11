package filters;

import models.ChunkBoundaries;
import models.ConcurrentImage;

import java.awt.*;
import java.util.Random;

public class GlassFilter {
    public static void exec(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg, int bias) {

        Random random = new Random();

        // Runs through entire matrix
        for (int x = boundaries.lowerBoundM(); x <= boundaries.upperBoundM(); x++) {
            for (int y = boundaries.lowerBoundN(); y <= boundaries.upperBoundN(); y++) {

                // gets a random neighbor pixel
                int minX = Math.max(x - bias, 0);
                int maxX = Math.min(x + bias, inputImg.length - 1);
                int mixY = Math.max(y - bias, 0);
                int maxY = Math.min(y + bias, inputImg[x].length - 1);

                int randomX = random.nextInt(minX, maxX);
                int randomY = random.nextInt(mixY, maxY);

                // replace the actual with that value
                Color pixel = inputImg[randomX][randomY];
                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();
                Color newPixel = new Color(r, g, b);

                sharedImg.updatePixel(x, y, newPixel);
            }
        }
    }
}
