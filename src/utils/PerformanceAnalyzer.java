package utils;

import models.ImageDivisionEnum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class PerformanceAnalyzer {

    private long startTime;
    private long endTime;
    private boolean isCounting;
    private final StringBuilder CSVBuilder = new StringBuilder();


    public void start(String method, String image, String filter) {
        CSVBuilder.append(method).append(";").append(image).append(";").append(filter).append(";");
        isCounting = true;
        startTime = System.nanoTime();
    }

    public void start(String method, String image, String filter, ImageDivisionEnum division, int val1) {
        CSVBuilder.append(method).append(";").append(image).append(";").append(filter).append(";").append(division)
                .append(";").append(val1).append(";");
        System.out.println(CSVBuilder);
        isCounting = true;
        startTime = System.nanoTime();
    }

    public void start(String method, String image, String filter, ImageDivisionEnum division, int val1, int val2) {
        CSVBuilder.append(method).append(";").append(image).append(";").append(filter).append(";").append(division)
                .append(";").append(val1).append(";").append(val2).append(";");
        System.out.println(CSVBuilder);
        isCounting = true;
        startTime = System.nanoTime();
    }

    public void stop() {
        if (isCounting) {
            this.isCounting = false;
            endTime = System.nanoTime();
            CSVBuilder.append(this.getElapsedTimeMillis()).append("\n");
        } else {
            throw new RuntimeException("the chronometer has not been started");
        }
    }

    public void save(String CSVPath) {
        try (PrintWriter CSVWriter = new PrintWriter(new FileWriter(CSVPath, false))) {
            CSVWriter.write(CSVBuilder.toString());
        } catch (IOException e) {
            System.err.println("IO operation error while saving the logs, error : " + e.getMessage());
        }
    }

    public long getElapsedTimeMillis() {
        return (endTime - startTime) / 1000000;
    }

    public void csvHeaders(String content) {
        CSVBuilder.append(content);
    }
}
