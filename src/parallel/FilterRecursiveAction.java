package parallel;

import filters.Filter;
import models.ChunkBoundaries;
import models.ConcurrentImage;
import models.ImageDivisionEnum;

import java.awt.*;
import java.util.concurrent.RecursiveAction;

import static utils.DataDecompositionUtils.getChunksOf;

public class FilterRecursiveAction extends RecursiveAction {
    private final Color[][] image;
    private final ChunkBoundaries chunkBoundaries;
    private final ConcurrentImage outputImg;
    private final ImageDivisionEnum divisionMethod;
    private final Filter filter;
    private final int threshold;
    private final int amount;

    public FilterRecursiveAction(Color[][] image, ChunkBoundaries chunkBoundaries, ConcurrentImage outputImg,
                                 ImageDivisionEnum divisionMethod, Filter filter, int threshold, int amount) {
        this.image = image;
        this.chunkBoundaries = chunkBoundaries;
        this.outputImg = outputImg;
        this.divisionMethod = divisionMethod;
        this.filter = filter;
        this.threshold = threshold;
        this.amount = amount;
    }

    @Override
    protected void compute() {
        int xLength = chunkBoundaries.upperBoundN() - chunkBoundaries.lowerBoundN() + 1;
        int yLength = chunkBoundaries.upperBoundM() - chunkBoundaries.lowerBoundM() + 1;
        int pixels = xLength * yLength;

        if (pixels <= threshold) {
            filter.applyFilter(image, chunkBoundaries, outputImg, amount);
        } else {
            ChunkBoundaries[] chunks;
            if (divisionMethod == ImageDivisionEnum.RECTANGULAR) {
                chunks = getChunksOf(chunkBoundaries, 4, divisionMethod);
            } else {
                chunks = getChunksOf(chunkBoundaries, 2, divisionMethod);
            }

            for (var chunk : chunks) {
                FilterRecursiveAction filterTask = new FilterRecursiveAction(image, chunk, outputImg, divisionMethod, filter, threshold, amount);
                invokeAll(filterTask);
            }
        }
    }
}