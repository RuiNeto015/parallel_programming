package utils;

import models.ChunkBoundaries;
import models.ImageDivisionEnum;

public class DataDecompositionUtils {

    private static ChunkBoundaries[] splitIntoVerticalChunks(ChunkBoundaries boundaries, int numOfChunks) {
        int nLength = boundaries.upperBoundN() - boundaries.lowerBoundN() + 1;

        int numOfColumnsPerChunk = nLength / numOfChunks;
        ChunkBoundaries[] result = new ChunkBoundaries[numOfChunks];
        int colIndex = boundaries.lowerBoundN();

        for (int i = 0; i < result.length - 1; i++) {
            result[i] = new ChunkBoundaries(boundaries.lowerBoundM(), boundaries.upperBoundM(), colIndex, colIndex + numOfColumnsPerChunk - 1);
            colIndex += numOfColumnsPerChunk;
        }

        result[result.length - 1] = new ChunkBoundaries(boundaries.lowerBoundM(), boundaries.upperBoundM(), colIndex, boundaries.upperBoundN());
        return result;
    }


    private static ChunkBoundaries[] splitIntoHorizontalChunks(ChunkBoundaries boundaries, int numOfChunks) {
        int mLength = boundaries.upperBoundM() - boundaries.lowerBoundM() + 1;

        int numOfRowsPerChunk = mLength / numOfChunks;
        ChunkBoundaries[] result = new ChunkBoundaries[numOfChunks];
        int rowIndex = boundaries.lowerBoundM();

        for (int i = 0; i < result.length - 1; i++) {
            result[i] = new ChunkBoundaries(rowIndex, rowIndex + numOfRowsPerChunk - 1, boundaries.lowerBoundN(), boundaries.upperBoundN());
            rowIndex += numOfRowsPerChunk;
        }

        result[result.length - 1] = new ChunkBoundaries(rowIndex, boundaries.upperBoundM(), boundaries.lowerBoundN(), boundaries.upperBoundN());
        return result;
    }


    private static ChunkBoundaries[] splitIntoRectangularChunks(ChunkBoundaries boundaries, int numOfChunks) {
        ChunkBoundaries[] result = new ChunkBoundaries[numOfChunks];
        int splitMatrixInNRows = 0;
        int splitMatrixInNColumns = 0;

        for (int i = 2; i <= Math.sqrt(numOfChunks); i++) {
            if (numOfChunks % i == 0) {
                splitMatrixInNColumns = i;
                splitMatrixInNRows = numOfChunks / i;
            }
        }

        int mLength = boundaries.upperBoundM() - boundaries.lowerBoundM() + 1;
        int nLength = boundaries.upperBoundN() - boundaries.lowerBoundN() + 1;

        int numOfRowsPerChunk = mLength / splitMatrixInNRows;
        int numOfRowsPerChunkReminder = mLength % splitMatrixInNRows;
        int numOfColsPerChunk = nLength / splitMatrixInNColumns;
        int numOfColsPerChunkReminder = nLength % splitMatrixInNColumns;
        int lastColChunkLb = nLength - (numOfColsPerChunk + numOfColsPerChunkReminder) + boundaries.lowerBoundN();
        int lastRowChunkLb = mLength - (numOfRowsPerChunk + numOfRowsPerChunkReminder) + boundaries.lowerBoundM();
        int rowIndex = boundaries.lowerBoundM();
        int colIndex = boundaries.lowerBoundN();

        for (int i = 0; i < result.length - 1; i++) {
            if (colIndex == lastColChunkLb) { // last column
                result[i] = new ChunkBoundaries(rowIndex, rowIndex + numOfRowsPerChunk - 1, colIndex, boundaries.upperBoundN());
                colIndex = boundaries.lowerBoundN();
                rowIndex += numOfRowsPerChunk;
            } else if (rowIndex == lastRowChunkLb) { // last row
                result[i] = new ChunkBoundaries(rowIndex, boundaries.upperBoundM(), colIndex, colIndex + numOfColsPerChunk - 1);
                colIndex += numOfColsPerChunk;
            } else {
                result[i] = new ChunkBoundaries(rowIndex, rowIndex + numOfRowsPerChunk - 1, colIndex, colIndex + numOfColsPerChunk - 1);
                colIndex += numOfColsPerChunk;
            }
        }

        result[result.length - 1] = new ChunkBoundaries(rowIndex, boundaries.upperBoundM(), colIndex, boundaries.upperBoundN());
        return result;
    }

    public static ChunkBoundaries[] getChunksOf(ChunkBoundaries boundaries, int numOfChunks, ImageDivisionEnum imageDivision) {
        return switch (imageDivision) {
            case VERTICAL -> splitIntoVerticalChunks(boundaries, numOfChunks);
            case HORIZONTAL -> splitIntoHorizontalChunks(boundaries, numOfChunks);
            case RECTANGULAR -> splitIntoRectangularChunks(boundaries, numOfChunks);
        };
    }
}