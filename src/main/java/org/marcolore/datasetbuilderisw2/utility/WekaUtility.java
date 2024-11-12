package org.marcolore.datasetbuilderisw2.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

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
        String arffFilePath;
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file.", e);
        }
        if(Objects.equals(properties.getProperty("path"), "2")) {
            arffFilePath = String.format("C:\\Users\\HP\\DatasetBuilderISW2\\src\\main\\dataset\\arffDataset\\%sDataset\\%s\\%s_%d_%s.arff"
                    ,
                    project.toLowerCase(), type, type, iteration, project.toLowerCase());
        } else {
            arffFilePath = String.format("C:\\Users\\Utente\\IdeaProjects\\isw2-definitivo\\src\\main\\dataset\\arffDataset\\%sDataset\\%s\\%s_%d_%s.arff"
                    ,
                    project.toLowerCase(), type, type, iteration, project.toLowerCase());
        }
        File arffFile = new File(arffFilePath);
        if (!arffFile.exists()) {
            throw new IllegalArgumentException("File ARFF doesn't exists: " + arffFilePath);
        }

        ArffLoader loader = new ArffLoader();
        loader.setSource(arffFile);

        return loader.getDataSet();
    }

}
