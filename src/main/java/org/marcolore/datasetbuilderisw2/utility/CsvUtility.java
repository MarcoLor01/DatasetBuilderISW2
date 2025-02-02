package org.marcolore.datasetbuilderisw2.utility;

import org.marcolore.datasetbuilderisw2.model.AcumeClass;
import org.marcolore.datasetbuilderisw2.model.EvaluationMetrics;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.marcolore.datasetbuilderisw2.model.ModelEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.gui.simplecli.Java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
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

        String fileName = String.format("%s_%d_%s.csv", title, number, projectName.toLowerCase());
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

    public static void createAcumeFiles(String project, List<AcumeClass> acumeClasses, boolean isBalancingMethod, boolean isFeatureSelection, boolean isCostSensitive, int iteration, String classifierName) {

        String basePath = "src/main/dataset/acumeDataset";

        String projectFolderName = project + "Dataset";
        String projectFolderPath = Paths.get(basePath, projectFolderName).toString();

        File projectFolder = new File(projectFolderPath);
        if (!projectFolder.exists()) {
            projectFolder.mkdirs();
        }

        StringBuilder booleanPart = new StringBuilder();
        if (isFeatureSelection) {
            booleanPart.append("BestFirst_");
        }
        if (isCostSensitive) {
            booleanPart.append("SensitiveThreshold_");
        }
        if (isBalancingMethod) {
            booleanPart.append("SMOTE_");
        }

        if (!booleanPart.isEmpty()) {
            booleanPart.setLength(booleanPart.length() - 1);
        }

        String fileName = project + "_" + classifierName + "_" + booleanPart + "_" + iteration + ".csv";
        String filePath = Paths.get(projectFolderPath, fileName).toString();

        try (FileWriter csvWriter = new FileWriter(filePath)) {

            csvWriter.append("InstanceId,Size,PredictedClassProbability,ActualClassLabel\n");

            for (AcumeClass acumeClass : acumeClasses) {
                csvWriter.append(String.valueOf(acumeClass.getInstanceId())).append(",").
                        append(String.valueOf(acumeClass.getSize())).append(",").
                        append(String.valueOf(acumeClass.getPredictedClassProbability())).append(",")
                        .append(String.valueOf(acumeClass.getActualClassLabel())).
                        append("\n");
            }

            logger.info("Creation of CSV file : {}", filePath);

        } catch (IOException e) {
            logger.error("Error in the writing of the file: {}", e.getMessage());
        }
    }

    public static void writeWekaFinalFile(String project, List<ModelEvaluation> evaluations) {
        String fullPath = "src/main/dataset/finalWekaResult/" + project + "finalResult" + ".csv";

        try (FileWriter writer = new FileWriter(fullPath)) {

            writer.append("Iteration,TrainingPercent,ClassifierName,FeatureSelection,Balancing,CostSensitive,");
            writer.append("Precision,Recall,AUC,Kappa,F1,TP,FP,TN,FN,Cost\n");

            for (ModelEvaluation evaluation : evaluations) {

                writer.append(String.valueOf(evaluation.getIteration())).append(",");
                writer.append(String.valueOf(evaluation.getTrainingPercent())).append(",");
                writer.append(WekaUtility.getClassifierName(evaluation.getClassifier())).append(",");
                writer.append(evaluation.getFeatureSelection()).append(",");
                writer.append(evaluation.getBalancingMethod()).append(",");
                writer.append(evaluation.getCostSensitive()).append(",");

                EvaluationMetrics metrics = evaluation.getEvaluationMetrics();
                writer.append(String.valueOf(metrics.getPrecision())).append(",");
                writer.append(String.valueOf(metrics.getRecall())).append(",");
                writer.append(String.valueOf(metrics.getAuc())).append(",");
                writer.append(String.valueOf(metrics.getKappa())).append(",");
                writer.append(String.valueOf(metrics.getF1())).append(",");
                writer.append(String.valueOf(metrics.getTp())).append(",");
                writer.append(String.valueOf(metrics.getFp())).append(",");
                writer.append(String.valueOf(metrics.getTn())).append(",");
                writer.append(String.valueOf(metrics.getFn())).append(",");
                writer.append(String.valueOf(metrics.getCost())).append("\n");
            }
            logger.info("Created final file for weka: {}", fullPath);
        } catch (IOException e) {
            logger.error("Errore: {}", e.getMessage());
        }
    }
}

