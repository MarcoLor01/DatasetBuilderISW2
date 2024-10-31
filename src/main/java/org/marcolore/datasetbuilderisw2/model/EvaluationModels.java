package org.marcolore.datasetbuilderisw2.model;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class EvaluationModels {

    Instances trainingSet;
    Instances testingSet;
    
    List<ConfiguredClassifier> configuredClassifiers = new ArrayList<>();

    public Instances getTrainingSet() {
        return trainingSet;
    }

    public void setTrainingSet(Instances trainingSet) {
        this.trainingSet = trainingSet;
    }

    public Instances getTestingSet() {
        return testingSet;
    }

    public void setTestingSet(Instances testingSet) {
        this.testingSet = testingSet;
    }

    public List<ConfiguredClassifier> getConfiguredClassifiers() {

        return configuredClassifiers;
    }

    public void setConfiguredClassifiers() {
        //TODO: Implementa logica di creazione dei modelli
    }

}
