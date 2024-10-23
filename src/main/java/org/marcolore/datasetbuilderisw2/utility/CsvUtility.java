package org.marcolore.datasetbuilderisw2.utility;

import org.marcolore.datasetbuilderisw2.model.JavaClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvUtility {

    public static void WriteCsvFile(List<JavaClass> javaClassList, String title, int number, String projectName) {
        System.out.printf("Creazione del dataset di %s\n", title);


        String baseDir = "src/main/dataset/csvDataset";
        File baseDirectory = new File(baseDir);

        if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
            System.err.println("La directory iniziale non esiste o non è una directory: " + baseDir);
            return;
        }

        String projectDir = baseDir + "/" + projectName + "Dataset/" + title;
        File directory = new File(projectDir);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Directory creata: " + directory.getAbsolutePath());
            } else {
                System.err.println("Impossibile creare la directory: " + directory.getAbsolutePath());
                return;
            }
        }

        String fileName = String.format("%s_%d_%s.csv", title, number, projectName);
        File csvFile = new File(directory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("className,loc,authorsNumber,revisionsNumber,touchedLoc,totalAddedLines,averageAddedLines,maxAddedLines,totalChurn,averageChurn,maxChurn,numberFix,cyclomaticComplexity,daysBetweenCommits,buggy");
            writer.newLine();

            for (JavaClass javaClass : javaClassList) {
                String line = String.format("\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%b",
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
                        javaClass.isBuggy());

                writer.write(line);
                writer.newLine();
            }

            System.out.println("File CSV creato: " + csvFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file CSV: " + e.getMessage());
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
