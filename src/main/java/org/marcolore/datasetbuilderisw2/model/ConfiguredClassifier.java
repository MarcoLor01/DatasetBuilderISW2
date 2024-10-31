package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.Classifier;

public class ConfiguredClassifier {
    Classifier classifier;
    boolean balancingMethod;
    boolean featureSelection;
    boolean costSensitive;

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

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
}
