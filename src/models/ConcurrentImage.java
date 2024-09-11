package models;

import java.awt.*;

public class ConcurrentImage {

    private Color[][] image;

    public ConcurrentImage(Color[][] image) {
        if (image == null || image.length == 0 || image[0].length == 0) {
            throw new RuntimeException("Invalid image");
        }
        this.image = image;
    }

    public void updatePixel(int x, int y, Color value) {
        if (x >= image.length || y >= image[0].length) {
            throw new RuntimeException("Exceed boundaries while updating concurrent image, error");
        }
        this.image[x][y] = value;
    }

    public Color[][] get() {
        return this.image;
    }

}
