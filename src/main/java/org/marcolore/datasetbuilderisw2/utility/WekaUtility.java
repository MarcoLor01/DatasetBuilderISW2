package org.marcolore.datasetbuilderisw2.utility;

import org.marcolore.datasetbuilderisw2.model.ConfiguredClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.IOException;

public class WekaUtility {

    private WekaUtility() {
    }

    private static final Logger logger = LoggerFactory.getLogger(WekaUtility.class);

    public static Instances convertData(String project, int iteration, String type) {
        try {
            Instances data = getInstances(project, iteration, type);


            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            System.out.println("Numero di istanze: " + data.numInstances());
            System.out.println("Attributi: " + data.numAttributes());
            System.out.println(data); // Stampa il contenuto del dataset

            return data;
        } catch(IOException e) {
            logger.error("Error in conversion of data from Arff to Instances", e);
        }
        return null;
    }

    private static Instances getInstances(String project, int iteration, String type) throws IOException {
        String arffFilePath = String.format("C:\\Users\\HP\\DatasetBuilderISW2\\src\\main\\dataset\\arffDataset\\%sDataset\\%s\\%s_%d_%s.arff"
                ,
                project.toLowerCase(), type, type, iteration, project.toLowerCase());

        File arffFile = new File(arffFilePath);
        if (!arffFile.exists()) {
            throw new IllegalArgumentException("File ARFF doesn't exists: " + arffFilePath);
        }

        ArffLoader loader = new ArffLoader();
        loader.setSource(arffFile);

        return loader.getDataSet();
    }

    public static void printConfiguredClassifier(ConfiguredClassifier configuredClassifier) {
        System.out.printf("Balancing method: %s\n", configuredClassifier.getBalancingMethod().toString());
        System.out.printf("Cost sensitive: %b\n", configuredClassifier.isCostSensitive());
        System.out.printf("Feature selection: %b\n", configuredClassifier.isFeatureSelection());
    }

}
