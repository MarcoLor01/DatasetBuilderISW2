package org.marcolore.datasetbuilderisw2.utility;

import org.marcolore.datasetbuilderisw2.model.JavaClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ArffUtility {

    public static void createArff(List<JavaClass> classes, String title, int numberTraining, String project) throws IOException {
        project = project.toLowerCase();
        File directory = createDirectory(project, title);
        String fileName = String.format("%s_%d_%s.csv", title, numberTraining, project);

        File arffFile = new File(directory, fileName);

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(arffFile))) {
            writeArffHeader(fileWriter, project, title, numberTraining);
            writeArffData(fileWriter, classes);
            System.out.println("File ARFF creato: " + arffFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del file ARFF: " + e.getMessage());
            throw e;
        }
    }

    private static File createDirectory(String project, String title) {

        String baseDir = "src/main/dataset/arffDataset";
        File baseDirectory = new File(baseDir);

        if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
            System.err.println("La directory iniziale non esiste o non è una directory: " + baseDir);
            return null;
        }

        String projectDir = baseDir + "/" + project + "Dataset/" + title;
        File dir = new File(projectDir);

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Directory creata: " + dir.getAbsolutePath());
            } else {
                System.err.println("Impossibile creare la directory: " + dir.getAbsolutePath());
                return null;
            }
        }
        return dir;
    }

    private static void writeArffHeader(BufferedWriter fileWriter, String project, String type, int numberTraining) throws IOException {
        fileWriter.append("@relation ").append(project).append("_").append(type).append("_").append(String.valueOf(numberTraining)).append("\n\n")
                .append("@attribute LOC numeric").append("\n")
                .append("@attribute authors_number numeric").append("\n")
                .append("@attribute revisions_number numeric").append("\n")
                .append("@attribute touched_loc numeric").append("\n")
                .append("@attribute total_add_lines numeric").append("\n")
                .append("@attribute average_add_lines numeric").append("\n")
                .append("@attribute max_add_lines numeric").append("\n")
                .append("@attribute total_churn numeric").append("\n")
                .append("@attribute max_churn numeric").append("\n")
                .append("@attribute number_fix numeric").append("\n")
                .append("@attribute cyclomatic_complexity numeric").append("\n")
                .append("@attribute average_churn numeric").append("\n")
                .append("@attribute buggy {'YES', 'NO'}").append("\n\n")
                .append("@data").append("\n");
    }

    private static void writeArffData(BufferedWriter fileWriter, List<JavaClass> classes) throws IOException {
        for (JavaClass javaClass : classes) {
            String buggy = javaClass.isBuggy() ? "YES" : "NO";
            String line = String.format("%d,%d,%d,%d,%d,%.2f,%d,%d,%d,%d,%.2f,%s",
                    javaClass.getLoc(),
                    javaClass.getAuthorsNumber(),
                    javaClass.getRevisionsNumber(),
                    javaClass.getTouchedLoc(),
                    javaClass.getTotalAddedLines(),
                    javaClass.getAverageAddedLines(),
                    javaClass.getMaxAddedLines(),
                    javaClass.getTotalChurn(),
                    javaClass.getNumberFix(),
                    javaClass.getCyclomaticComplexity(),
                    javaClass.getAverageChurn(),
                    buggy);
            fileWriter.append(line).append("\n");
        }
    }
}

