package filters;

import models.ChunkBoundaries;
import models.ConcurrentImage;

import java.awt.*;

public class ConditionalBlurFilter {
    public static void exec(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg, int blurAmount) {
        for (int image_i = boundaries.lowerBoundM(); image_i <= boundaries.upperBoundM(); image_i++) {
            for (int image_j = boundaries.lowerBoundN(); image_j <= boundaries.upperBoundN(); image_j++) {
                if (inputImg[image_i][image_j].getRed() > 100) {
                    int sumRed = 0, sumGreen = 0, sumBlue = 0;
                    int count = 0;

                    for (int submatrix_i = -blurAmount; submatrix_i <= blurAmount; submatrix_i++) {
                        for (int submatrix_j = -blurAmount; submatrix_j <= blurAmount; submatrix_j++) {
                            int neighbor_i = image_i + submatrix_i;
                            int neighbor_j = image_j + submatrix_j;

                            if (neighbor_i >= 0 && neighbor_i < inputImg.length && neighbor_j >= 0 && neighbor_j
                                    < inputImg[0].length) {
                                Color pixel = inputImg[neighbor_i][neighbor_j];
                                sumRed += pixel.getRed();
                                sumGreen += pixel.getGreen();
                                sumBlue += pixel.getBlue();
                                count++;
                            }
                        }
                    }

                    int averageRed = sumRed / count;
                    int averageGreen = sumGreen / count;
                    int averageBlue = sumBlue / count;

                    Color newPixel = new Color(averageRed, averageGreen, averageBlue);

                    sharedImg.updatePixel(image_i, image_j, newPixel);
                }
            }
        }
    }
}
