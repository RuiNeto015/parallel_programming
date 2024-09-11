package filters;

import models.ChunkBoundaries;
import models.ConcurrentImage;

import java.awt.*;

public class SwirlFilter {
    public static void exec(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg, int swirlAmount) {
        int xCenter = inputImg.length / 2;
        int yCenter = inputImg[0].length / 2;

        for (int i = boundaries.lowerBoundM(); i <= boundaries.upperBoundM(); i++) {
            for (int j = boundaries.lowerBoundN(); j <= boundaries.upperBoundN(); j++) {
                double distance = Math.sqrt(Math.pow((i - xCenter), 2) + Math.pow((j - yCenter), 2));
                double angle = (Math.PI / 256) * distance * swirlAmount;

                int x = (int) ((i - xCenter) * Math.cos(angle) - (j - yCenter) * Math.sin(angle) + xCenter);
                int y = (int) ((i - xCenter) * Math.sin(angle) + (j - yCenter) * Math.cos(angle) + yCenter);

                if (x < 0) x *= -1;
                if (y < 0) y *= -1;

                if (x < inputImg.length && y < inputImg[i].length) {
                    Color newPixel = new Color(inputImg[x][y].getRed(), inputImg[x][y].getGreen(), inputImg[x][y].getBlue());
                    sharedImg.updatePixel(i, j, newPixel);
                }
            }
        }
    }
}
