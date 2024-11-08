package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;

public class ConfiguredClassifier {

    IBk ibk;
    RandomForest randomForest;
    NaiveBayes naiveBayes;
    boolean balancingMethod;
    boolean featureSelection;
    boolean costSensitive;


    public boolean isBalancingMethod() {
        return balancingMethod;
    }

    public void setBalancingMethod(boolean balancingMethod) {
        this.balancingMethod = balancingMethod;
    }

    public boolean isFeatureSelection() {
        return featureSelection;
    }

    public void setFeatureSelection(boolean featureSelection) {
        this.featureSelection = featureSelection;
    }

    public boolean isCostSensitive() {
        return costSensitive;
    }

    public void setCostSensitive(boolean costSensitive) {
        this.costSensitive = costSensitive;
    }

    public IBk getIbk() {
        return ibk;
    }

    public void setIbk(IBk ibk) {
        this.ibk = ibk;
    }

    public RandomForest getRandomForest() {
        return randomForest;
    }

    public void setRandomForest(RandomForest randomForest) {
        this.randomForest = randomForest;
    }

    public NaiveBayes getNaiveBayes() {
        return naiveBayes;
    }

    public void setNaiveBayes(NaiveBayes naiveBayes) {
        this.naiveBayes = naiveBayes;
    }

    public void prepareClassifier() {

    }
}
