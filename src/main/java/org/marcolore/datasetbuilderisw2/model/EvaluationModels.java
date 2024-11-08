package org.marcolore.datasetbuilderisw2.model;
import org.marcolore.datasetbuilderisw2.utility.ClassifierUtility;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.classifiers.lazy.IBk;

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

    private ConfiguredClassifier createConfiguredClassifier(IBk ibk, RandomForest randomForest, NaiveBayes naiveBayes,
                                                            boolean balancing, boolean costSensitive, boolean featureSelection) {
        ConfiguredClassifier configuredClassifier = new ConfiguredClassifier();

        configuredClassifier.setIbk(ibk);
        configuredClassifier.setRandomForest(randomForest);
        configuredClassifier.setNaiveBayes(naiveBayes);

        configuredClassifier.setBalancingMethod(balancing);
        configuredClassifier.setCostSensitive(costSensitive);
        configuredClassifier.setFeatureSelection(featureSelection);

        return configuredClassifier;
    }

    public void setConfiguredClassifiers() {

        boolean[] balancingOptions = {false, true};
        boolean[] costSensitiveOptions = {false, true};
        boolean[] featureSelectionOptions = {false, true};

        for (boolean balancing : balancingOptions) {
            for (boolean costSensitive : costSensitiveOptions) {
                for (boolean featureSelection : featureSelectionOptions) {

                    IBk ibk = ClassifierUtility.setIbk();
                    RandomForest randomForest = new RandomForest();
                    NaiveBayes naiveBayes = new NaiveBayes();

                    ConfiguredClassifier configuredClassifier = createConfiguredClassifier(
                            ibk, randomForest, naiveBayes,
                            balancing, costSensitive, featureSelection
                    );

                    configuredClassifiers.add(configuredClassifier);
                }
            }
        }
    }

}
