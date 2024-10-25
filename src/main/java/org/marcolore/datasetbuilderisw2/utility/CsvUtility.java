package org.marcolore.datasetbuilderisw2.utility;

import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtility {

    private CsvUtility() {
    }

    private static final Logger logger = LoggerFactory.getLogger(CsvUtility.class);

    public static void writeCsvFile(List<JavaClass> javaClassList, String title, int number, String projectName) {

        String baseDir = "src/main/dataset/csvDataset";
        File baseDirectory = new File(baseDir);

        if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
            logger.error("Start directory doesn't exist: {}", baseDir);
            return;
        }

        String projectDir = baseDir + "/" + projectName + "Dataset/" + title;
        File directory = new File(projectDir);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.info("Directory created: {}", directory.getAbsolutePath());
            } else {
                logger.error("Impossible to create the directory: {}", directory.getAbsolutePath());
                return;
            }
        }

        String fileName = String.format("%s_%d_%s.csv", title, number, projectName);
        File csvFile = new File(directory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("className,loc,authorsNumber,revisionsNumber,touchedLoc,totalAddedLines,averageAddedLines,maxAddedLines,totalChurn,maxChurn,numberFix,cyclomaticComplexity,averageChurn,daysBetweenCommits,buggy");
            writer.newLine();

            for (JavaClass javaClass : javaClassList) {
                String line = String.format("\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%b",
                        escapeCsv(javaClass.getClassName()),
                        javaClass.getLoc(),
                        javaClass.getAuthorsNumber(),
                        javaClass.getRevisionsNumber(),
                        javaClass.getTouchedLoc(),
                        javaClass.getTotalAddedLines(),
                        javaClass.getAverageAddedLines(),
                        javaClass.getMaxAddedLines(),
                        javaClass.getTotalChurn(),
                        javaClass.getMaxChurn(),
                        javaClass.getNumberFix(),
                        javaClass.getCyclomaticComplexity(),
                        javaClass.getAverageChurn(),
                        javaClass.getDaysBetweenCommits(),
                        javaClass.isBuggy());

                writer.write(line);
                writer.newLine();
            }

            logger.info("CSV file created: {}", csvFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error in the writing: {}", e.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}

