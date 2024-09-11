import filters.*;
import models.ChunkBoundaries;
import models.ConcurrentImage;
import models.FiltersEnum;
import models.ImageDivisionEnum;
import parallel.FilterCallable;
import utils.ImageUtils;
import utils.PerformanceAnalyzer;

import java.awt.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static models.FiltersEnum.*;
import static models.ImageDivisionEnum.*;

public class ExecutorBasedImpl {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // CONFIG
        final int BLUR_AMOUNT = 5;
        final int BRIGHTNESS_AMOUNT = 70;
        final int CONDITIONAL_BLUR_AMOUNT = 10;
        final int GLASS_BIAS = 2;
        final int GRAYSCALE_CONTRAST = 5;
        final int SWIRL_AMOUNT = 2;
        final List<Integer> turtleThresholds = List.of(121104, 90828, 72662);
        final List<Integer> monkeyThresholds = List.of(87381, 65536, 52428);
        final List<Integer> bridgeThresholds = List.of(8004000, 6003000, 4802400);
        final List<Integer> numOfThreads = List.of(8, 9, 10);
        final List<ImageDivisionEnum> divisionConf = List.of(VERTICAL, HORIZONTAL, RECTANGULAR);
        final List<FiltersEnum> filtersConf = List.of(BLUR, BRIGHTNESS, CONDITIONAL_BLUR, GLASS, GRAYSCALE, SWIRL);
        final int RUNS = 3;

        // FILES PATH
        final String inputDirPath = "./input/";
        final String outputDirPath = "./output/";
        final String outputCSVPath = "./output/executor_based.csv";

        // IMAGES TO PROCESS
        final List<String> imgPathList = List.of(inputDirPath + "turtle.jpg", inputDirPath + "monkey.png", inputDirPath + "bridge.jpg");

        // PERFORMANCE ANALYZER
        PerformanceAnalyzer performanceAnalyzer = new PerformanceAnalyzer();
        performanceAnalyzer.csvHeaders("Method;Image;Filter;Image Division Method;Number of Threads;Threshold(px);Time(ms)\n");

        for (String imgPath : imgPathList) {
            String imageName = imgPath.substring(imgPath.lastIndexOf("/") + 1, imgPath.lastIndexOf("."));
            Color[][] image = ImageUtils.loadImage(imgPath);
            ChunkBoundaries imageAsChunk = new ChunkBoundaries(0, image.length - 1, 0, image[0].length - 1);

            for (int t = 0; t < 3; t++) {
                for (var currentNumOfThreads : numOfThreads) {
                    ExecutorService executor = Executors.newFixedThreadPool(currentNumOfThreads);

                    for (var divisionMethod : divisionConf) {
                        var THRESHOLD = switch (imageName) {
                            case "turtle" -> turtleThresholds.get(t);
                            case "monkey" -> monkeyThresholds.get(t);
                            case "bridge" -> bridgeThresholds.get(t);
                            default -> throw new IllegalStateException("Missing threshold for image");
                        };

                        for (var filter : filtersConf) {
                            String outputPath = outputDirPath + imageName + "_" + filter.name().toLowerCase() + "_executor_based.jpg";
                            ConcurrentImage outputImg = new ConcurrentImage(ImageUtils.copyImage(image));

                            for (int k = 0; k < RUNS; k++) {
                                performanceAnalyzer.start("executor-based", imgPath, filter.name().toLowerCase(), divisionMethod,
                                        currentNumOfThreads, THRESHOLD);

                                Callable<Void> callable = switch (filter) {
                                    case BLUR -> new FilterCallable(image, imageAsChunk, outputImg, divisionMethod,
                                            executor, BlurFilter::exec, THRESHOLD, BLUR_AMOUNT);

                                    case BRIGHTNESS ->
                                            new FilterCallable(image, imageAsChunk, outputImg, divisionMethod,
                                                    executor, BrightnessFilter::exec, THRESHOLD, BRIGHTNESS_AMOUNT);

                                    case CONDITIONAL_BLUR ->
                                            new FilterCallable(image, imageAsChunk, outputImg, divisionMethod,
                                                    executor, ConditionalBlurFilter::exec, THRESHOLD, CONDITIONAL_BLUR_AMOUNT);

                                    case GLASS -> new FilterCallable(image, imageAsChunk, outputImg, divisionMethod,
                                            executor, GlassFilter::exec, THRESHOLD, GLASS_BIAS);

                                    case GRAYSCALE -> new FilterCallable(image, imageAsChunk, outputImg, divisionMethod,
                                            executor, GrayscaleFilter::exec, THRESHOLD, GRAYSCALE_CONTRAST);

                                    case SWIRL -> new FilterCallable(image, imageAsChunk, outputImg, divisionMethod,
                                            executor, SwirlFilter::exec, THRESHOLD, SWIRL_AMOUNT);
                                };

                                executor.submit(callable).get();
                                ImageUtils.writeImage(outputImg.get(), outputPath);
                                performanceAnalyzer.stop();
                            }
                        }
                    }
                    executor.shutdown();
                }
            }
        }
        performanceAnalyzer.save(outputCSVPath);
    }
}