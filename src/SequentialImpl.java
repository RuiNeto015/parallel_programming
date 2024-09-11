import filters.*;
import models.ChunkBoundaries;
import models.ConcurrentImage;
import models.FiltersEnum;
import utils.ImageUtils;
import utils.PerformanceAnalyzer;

import java.awt.*;
import java.util.List;

import static models.FiltersEnum.*;
import static models.FiltersEnum.SWIRL;

public class SequentialImpl {
    public static void main(String[] args) {
        // GENERAL CONFIG
        final int BLUR_AMOUNT = 5;
        final int BRIGHTNESS_AMOUNT = 70;
        final int CONDITIONAL_BLUR_AMOUNT = 10;
        final int GLASS_BIAS = 2;
        final int GRAYSCALE_CONTRAST = 5;
        final int SWIRL_AMOUNT = 2;
        final List<FiltersEnum> filtersConf = List.of(BLUR, BRIGHTNESS, CONDITIONAL_BLUR, GLASS, GRAYSCALE, SWIRL);
        final int RUNS = 3;

        // FILES PATH
        final String inputDirPath = "./input/";
        final String outputDirPath = "./output/";
        final String outputLogFile = "./output/log.txt";
        final String outputCSVPath = "./output/sequential.csv";

        // IMAGES TO PROCESS
        final List<String> imgPathList = List.of(inputDirPath + "turtle.jpg", inputDirPath + "monkey.png", inputDirPath + "bridge.jpg");

        // PERFORMANCE ANALYZER
        PerformanceAnalyzer performanceAnalyzer = new PerformanceAnalyzer();
        performanceAnalyzer.csvHeaders("Method; Image; Filter; Time(ms)\n");

        for (String imgPath : imgPathList) {
            String imageName = imgPath.substring(imgPath.lastIndexOf("/") + 1, imgPath.lastIndexOf("."));
            Color[][] image = ImageUtils.loadImage(imgPath);
            ChunkBoundaries imageAsChunk = new ChunkBoundaries(0, image.length - 1, 0, image[0].length - 1);

            for (var filter : filtersConf) {
                String outputPath = outputDirPath + imageName + "_" + filter.name().toLowerCase() + "_sequential.jpg";

                for (int i = 0; i < RUNS; i++) {
                    ConcurrentImage outputImg = new ConcurrentImage(ImageUtils.copyImage(image));
                    performanceAnalyzer.start("sequential", imgPath, filter.name().toLowerCase());

                    switch (filter) {
                        case BLUR -> BlurFilter.exec(image, imageAsChunk, outputImg, BLUR_AMOUNT);
                        case BRIGHTNESS -> BrightnessFilter.exec(image, imageAsChunk, outputImg, BRIGHTNESS_AMOUNT);
                        case CONDITIONAL_BLUR ->
                                ConditionalBlurFilter.exec(image, imageAsChunk, outputImg, CONDITIONAL_BLUR_AMOUNT);
                        case GLASS -> GlassFilter.exec(image, imageAsChunk, outputImg, GLASS_BIAS);
                        case GRAYSCALE -> GrayscaleFilter.exec(image, imageAsChunk, outputImg, GRAYSCALE_CONTRAST);
                        case SWIRL -> SwirlFilter.exec(image, imageAsChunk, outputImg, SWIRL_AMOUNT);
                    }

                    ImageUtils.writeImage(outputImg.get(), outputPath);
                    performanceAnalyzer.stop();
                }
            }
        }
        performanceAnalyzer.save(outputCSVPath);
    }
}
