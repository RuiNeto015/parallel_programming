import filters.*;
import models.ChunkBoundaries;
import models.ConcurrentImage;
import models.FiltersEnum;
import models.ImageDivisionEnum;
import utils.ImageUtils;
import utils.PerformanceAnalyzer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static models.FiltersEnum.*;
import static models.ImageDivisionEnum.*;
import static utils.DataDecompositionUtils.getChunksOf;

public class CompletableFuturesImpl {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // GENERAL CONFIG
        final int BLUR_AMOUNT = 5;
        final int BRIGHTNESS_AMOUNT = 70;
        final int CONDITIONAL_BLUR_AMOUNT = 10;
        final int GLASS_BIAS = 2;
        final int GRAYSCALE_CONTRAST = 5;
        final int SWIRL_AMOUNT = 2;
        final List<Integer> numOfChunks = List.of(4, 8, 12, 16);
        final List<Integer> numOfThreads = List.of(8, 9, 10);
        final List<ImageDivisionEnum> divisionConf = List.of(VERTICAL, HORIZONTAL, RECTANGULAR);
        final List<FiltersEnum> filtersConf = List.of(BLUR, BRIGHTNESS, CONDITIONAL_BLUR, GLASS, GRAYSCALE, SWIRL);
        final int RUNS = 3;

        // FILES PATH
        final String inputDirPath = "./input/";
        final String outputDirPath = "./output/";
        final String outputCSVPath = "./output/completable_futures.csv";

        // IMAGES TO PROCESS
        final List<String> imgPathList = List.of(inputDirPath + "turtle.jpg", inputDirPath + "monkey.png", inputDirPath + "bridge.jpg");

        // PERFORMANCE ANALYZER
        PerformanceAnalyzer performanceAnalyzer = new PerformanceAnalyzer();
        performanceAnalyzer.csvHeaders("Method;Image;Filter;Image Division Method;Number of Threads;Number of chunks;Time(ms)\n");

        for (String imgPath : imgPathList) {
            String imageName = imgPath.substring(imgPath.lastIndexOf("/") + 1, imgPath.lastIndexOf("."));
            Color[][] image = ImageUtils.loadImage(imgPath);
            ChunkBoundaries imageAsChunk = new ChunkBoundaries(0, image.length - 1, 0, image[0].length - 1);

            for (var currentNumOfChunks : numOfChunks) {
                for (var currentNumOfThreads : numOfThreads) {
                    for (var divisionMethod : divisionConf) {
                        for (var filter : filtersConf) {
                            for (int k = 0; k < RUNS; k++) {
                                ExecutorService executor = Executors.newFixedThreadPool(currentNumOfThreads);
                                String outputPath = outputDirPath + imageName + "_" + filter.name().toLowerCase() + "_completable_features.jpg";
                                ConcurrentImage outputImg = new ConcurrentImage(ImageUtils.copyImage(image));
                                ChunkBoundaries[] chunks = getChunksOf(imageAsChunk, currentNumOfChunks, divisionMethod);
                                List<CompletableFuture<Void>> chunkFutures = new ArrayList<>();

                                performanceAnalyzer.start("completable_futures", imgPath, filter.name().toLowerCase(),
                                        divisionMethod, currentNumOfThreads, currentNumOfChunks);

                                for (ChunkBoundaries chunk : chunks) {
                                    CompletableFuture<Void> chunkFuture = CompletableFuture.runAsync(() -> {
                                        switch (filter) {
                                            case BLUR -> BlurFilter.exec(image, chunk, outputImg, BLUR_AMOUNT);
                                            case BRIGHTNESS ->
                                                    BrightnessFilter.exec(image, chunk, outputImg, BRIGHTNESS_AMOUNT);
                                            case CONDITIONAL_BLUR ->
                                                    ConditionalBlurFilter.exec(image, chunk, outputImg, CONDITIONAL_BLUR_AMOUNT);
                                            case GLASS -> GlassFilter.exec(image, chunk, outputImg, GLASS_BIAS);
                                            case GRAYSCALE ->
                                                    GrayscaleFilter.exec(image, chunk, outputImg, GRAYSCALE_CONTRAST);
                                            case SWIRL -> SwirlFilter.exec(image, chunk, outputImg, SWIRL_AMOUNT);
                                        }
                                    }, executor);

                                    chunkFutures.add(chunkFuture);
                                }

                                 var future = CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
                                    ImageUtils.writeImage(outputImg.get(), outputPath);
                                    performanceAnalyzer.stop();
                                }, executor);

                                future.get();
                                executor.shutdown();
                            }
                        }
                    }
                }
            }
            performanceAnalyzer.save(outputCSVPath);
        }
    }
}