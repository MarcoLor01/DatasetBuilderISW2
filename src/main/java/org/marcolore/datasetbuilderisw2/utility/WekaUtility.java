package org.marcolore.datasetbuilderisw2.utility;

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
            String arffFilePath = String.format("..\\dataset\\arffDataset\\%sDataset\\%s\\%s_%d_%s.arff",
                    project.toLowerCase(), type, type, iteration, project.toLowerCase());
            // Crea un oggetto ArffLoader
            ArffLoader loader = new ArffLoader();

            // Imposta il file ARFF da caricare
            loader.setSource(new File(arffFilePath));

            // Carica i dati in un oggetto Instances
            Instances data = loader.getDataSet();

            // Se il file ARFF ha una classe definita, imposta la classe da prevedere
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1); // Imposta l'ultimo attributo come classe
            }

            // Stampa le informazioni sui dati caricati
            System.out.println("Numero di istanze: " + data.numInstances());
            System.out.println("Attributi: " + data.numAttributes());
            System.out.println(data); // Stampa il contenuto del dataset
            return data;
        } catch(IOException e) {
            logger.error("Error in conversion of data from Arff to Instances");
        }
        return null;
    }


}
