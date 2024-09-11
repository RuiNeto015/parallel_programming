import filters.*;
import models.ChunkBoundaries;
import models.ConcurrentImage;
import models.FiltersEnum;
import models.ImageDivisionEnum;
import utils.ImageUtils;
import utils.PerformanceAnalyzer;

import java.awt.*;
import java.util.List;

import static models.FiltersEnum.*;
import static models.ImageDivisionEnum.*;
import static utils.DataDecompositionUtils.getChunksOf;

public class MultithreadedImpl {
    public static void main(String[] args) {
        // GENERAL CONFIG
        final int BLUR_AMOUNT = 5;
        final int BRIGHTNESS_AMOUNT = 70;
        final int CONDITIONAL_BLUR_AMOUNT = 10;
        final int GLASS_BIAS = 2;
        final int GRAYSCALE_CONTRAST = 5;
        final int SWIRL_AMOUNT = 2;
        final List<Integer> numOfThreads = List.of(8, 9, 10);
        final List<ImageDivisionEnum> divisionConf = List.of(VERTICAL, HORIZONTAL, RECTANGULAR);
        final List<FiltersEnum> filtersConf = List.of(BLUR, BRIGHTNESS, CONDITIONAL_BLUR, GLASS, GRAYSCALE, SWIRL);
        final int RUNS = 3;

        // FILES PATH
        final String inputDirPath = "./input/";
        final String outputDirPath = "./output/";
        final String outputCSVPath = "./output/multithreading.csv";

        // IMAGES TO PROCESS
        final List<String> imgPathList = List.of(inputDirPath + "turtle.jpg", inputDirPath + "monkey.png", inputDirPath + "bridge.jpg");

        // PERFORMANCE ANALYZER
        PerformanceAnalyzer performanceAnalyzer = new PerformanceAnalyzer();
        performanceAnalyzer.csvHeaders("Method; Image; Filter; Image Division Method; Number of Threads; Time(ms)\n");

        for (String imgPath : imgPathList) {
            String imageName = imgPath.substring(imgPath.lastIndexOf("/") + 1, imgPath.lastIndexOf("."));
            Color[][] image = ImageUtils.loadImage(imgPath);
            ChunkBoundaries imageAsChunk = new ChunkBoundaries(0, image.length - 1, 0, image[0].length - 1);

            for (var currentNumOfThreads : numOfThreads) {
                for (var divisionMethod : divisionConf) {
                    for (var filter : filtersConf) {
                        String outputPath = outputDirPath + imageName + "_" + filter.name().toLowerCase() + "_multithreaded.jpg";
                        ConcurrentImage outputImg = new ConcurrentImage(ImageUtils.copyImage(image));
                        ChunkBoundaries[] chunks = getChunksOf(imageAsChunk, currentNumOfThreads, divisionMethod);
                        Thread[] threads = new Thread[currentNumOfThreads];

                        for (int k = 0; k < RUNS; k++) {
                            performanceAnalyzer.start("multithreading", imgPath, filter.name().toLowerCase(), divisionMethod, currentNumOfThreads);

                            for (int i = 0; i < threads.length; i++) {
                                var chunk = chunks[i];

                                Runnable runnable = switch (filter) {
                                    case BLUR -> () -> BlurFilter.exec(image, chunk, outputImg, BLUR_AMOUNT);
                                    case BRIGHTNESS ->
                                            () -> BrightnessFilter.exec(image, chunk, outputImg, BRIGHTNESS_AMOUNT);
                                    case CONDITIONAL_BLUR ->
                                            () -> ConditionalBlurFilter.exec(image, chunk, outputImg, CONDITIONAL_BLUR_AMOUNT);
                                    case GLASS -> () -> GlassFilter.exec(image, chunk, outputImg, GLASS_BIAS);
                                    case GRAYSCALE ->
                                            () -> GrayscaleFilter.exec(image, chunk, outputImg, GRAYSCALE_CONTRAST);
                                    case SWIRL -> () -> SwirlFilter.exec(image, chunk, outputImg, SWIRL_AMOUNT);
                                };

                                threads[i] = new Thread(runnable);
                                threads[i].start();
                            }

                            try {
                                for (Thread thread : threads) {
                                    thread.join();
                                }
                            } catch (Exception e) {
                                System.out.println("Interrupted!");
                            }

                            ImageUtils.writeImage(outputImg.get(), outputPath);
                            performanceAnalyzer.stop();
                        }
                    }
                }
            }
        }
        performanceAnalyzer.save(outputCSVPath);
    }
}
