package org.marcolore.datasetbuilderisw2.model;
import org.marcolore.datasetbuilderisw2.utility.ClassifierUtility;
import weka.attributeSelection.BestFirst;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.filters.supervised.instance.SMOTE;
import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import weka.filters.supervised.attribute.AttributeSelection;
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
        configuredClassifier.addClassifier(ibk);
        configuredClassifier.addClassifier(randomForest);
        configuredClassifier.addClassifier(naiveBayes);

        configuredClassifier.setBalancingMethod(balancing);
        configuredClassifier.setCostSensitive(costSensitive);
        configuredClassifier.setFeatureSelection(featureSelection);

        return configuredClassifier;
    }

    public void setConfiguredClassifiers() {
        boolean[] costSensitiveOptions = {false, true};
        boolean[] featureSelectionOptions = {false, true};
        boolean[] balanceMethodOptions = {false, true};

        for (boolean balancing : balanceMethodOptions) {
            for (boolean costSensitive : costSensitiveOptions) {
                for (boolean featureSelection : featureSelectionOptions) {

                    if (balancing && costSensitive) {
                        continue;
                    }

                    IBk ibk = ClassifierUtility.setIbk();
                    RandomForest randomForest = new RandomForest();
                    NaiveBayes naiveBayes = new NaiveBayes();

                    ConfiguredClassifier configuredClassifier = createConfiguredClassifier(
                            ibk, randomForest, naiveBayes,
                            balancing, costSensitive, featureSelection
                    );
                    
                    prepareClassifier(configuredClassifier);
                }
            }
        }
    }

    void prepareClassifier(ConfiguredClassifier configuredClassifier) {

        int numberOfAttributes = trainingSet.numAttributes();
        AttributeStats attributeStats = trainingSet.attributeStats(numberOfAttributes-1);
        int sizeMajorClass = attributeStats.nominalCounts[1];
        int sizeMinorClass = attributeStats.nominalCounts[0];

        if (sizeMinorClass == 0) {
            throw new IllegalArgumentException("Minor class has no instances in the training set.");
        }

        for(Classifier classifier : configuredClassifier.getBaseClassifierList()) {
            Classifier currentClassifier = classifier;

            if (configuredClassifier.isCostSensitive()) {
                CostSensitiveClassifier costSensitiveClassifier = createModelCostSensitive();
                costSensitiveClassifier.setClassifier(currentClassifier);
                currentClassifier = costSensitiveClassifier;
                System.out.print("Sono in costSensitive\n");
            }

            if(configuredClassifier.isFeatureSelection()) {
                currentClassifier = createModelFeatureSelection(currentClassifier);
                System.out.print("Sono in featureSelection\n");
            }

            if(configuredClassifier.isBalancingMethod()) {
                currentClassifier = createModelSMOTE(currentClassifier, sizeMajorClass, sizeMinorClass);
                System.out.print("Sono in balancingMethod\n");
            }

            configuredClassifier.addReadyClassifier(currentClassifier);
        }

        this.configuredClassifiers.add(configuredClassifier);

    }


    private Classifier createModelSMOTE(Classifier classifier, int sizeMajorClass, int sizeMinorClass) {
        double percentageSmote = (100.0 * (sizeMajorClass - sizeMinorClass)) / sizeMinorClass;
        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);
        SMOTE smote = new SMOTE();
        smote.setPercentage(percentageSmote);
        fc.setFilter(smote);
        return fc;
    }

    private FilteredClassifier createModelFeatureSelection(Classifier classifier) {
        AttributeSelection attributeSelection = new AttributeSelection();
        BestFirst bestFirst = new BestFirst();
        bestFirst.setDirection(new SelectedTag(0, bestFirst.getDirection().getTags()));
        attributeSelection.setSearch(bestFirst);

        FilteredClassifier fc = new FilteredClassifier();
        fc.setClassifier(classifier);
        fc.setFilter(attributeSelection);

        return fc;
    }

    private CostSensitiveClassifier createModelCostSensitive() {

        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        CostMatrix costMatrix = new CostMatrix(2);
        costMatrix.setCell(0,0,0.0);
        costMatrix.setCell(1,0,1.0);
        costMatrix.setCell(0,1,10.0);
        costMatrix.setCell(1,1,0.0);
        costSensitiveClassifier.setMinimizeExpectedCost(false);

        costSensitiveClassifier.setCostMatrix(costMatrix);
        return costSensitiveClassifier;
    }


}
