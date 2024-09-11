package filters;

import models.ChunkBoundaries;
import models.ConcurrentImage;

import java.awt.*;

@FunctionalInterface
public interface Filter {
    void applyFilter(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg, int amount);
}
