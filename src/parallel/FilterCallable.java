package parallel;

import filters.Filter;
import models.ChunkBoundaries;
import models.ConcurrentImage;
import models.ImageDivisionEnum;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static utils.DataDecompositionUtils.getChunksOf;

public class FilterCallable implements Callable<Void> {

    private final Color[][] inputImg;
    private final ChunkBoundaries boundaries;
    private final ConcurrentImage sharedImg;
    private final ImageDivisionEnum imgDivision;
    private final ExecutorService executor;
    private final Filter filter;
    private final int threshold;
    private final int amount;

    public FilterCallable(Color[][] inputImg, ChunkBoundaries boundaries, ConcurrentImage sharedImg,
                          ImageDivisionEnum imgDivision, ExecutorService executor, Filter filter, int threshold, int amount) {
        this.inputImg = inputImg;
        this.boundaries = boundaries;
        this.sharedImg = sharedImg;
        this.imgDivision = imgDivision;
        this.executor = executor;
        this.filter = filter;
        this.threshold = threshold;
        this.amount = amount;
    }

    @Override
    public Void call() throws ExecutionException, InterruptedException {
        int xLength = boundaries.upperBoundN() - boundaries.lowerBoundN() + 1;
        int yLength = boundaries.upperBoundM() - boundaries.lowerBoundM() + 1;
        int pixels = xLength * yLength;
        List<Future<Void>> results = new ArrayList<>();

        if (pixels < threshold) {
            filter.applyFilter(inputImg, boundaries, sharedImg, amount);
        } else {
            ChunkBoundaries[] chunks;
            if (imgDivision == ImageDivisionEnum.RECTANGULAR) {
                chunks = getChunksOf(boundaries, 4, imgDivision);
            } else {
                chunks = getChunksOf(boundaries, 2, imgDivision);
            }

            for (var chunk : chunks) {
                FilterCallable nextTask = new FilterCallable(inputImg, chunk, sharedImg, imgDivision, executor,
                        filter, threshold, amount);
                results.add(executor.submit(nextTask));
            }

            for (var result : results) {
                result.get();
            }
        }
        return null;
    }
}
