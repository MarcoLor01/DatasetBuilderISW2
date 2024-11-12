package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.Classifier;

import java.util.ArrayList;
import java.util.List;

public class ConfiguredClassifier {

    private List<Classifier> baseClassifierList = new ArrayList<>();
    private List<Classifier> readyClassifierList = new ArrayList<>();
    private boolean balancingMethod;
    private boolean featureSelection;
    private boolean costSensitive;

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

    public List<Classifier> getBaseClassifierList() {
        return baseClassifierList;
    }

    public void setBaseClassifierList(List<Classifier> baseClassifierList) {
        this.baseClassifierList = baseClassifierList;
    }

    public void addClassifier(Classifier classifier) {
        this.baseClassifierList.add(classifier);
    }

    public List<Classifier> getReadyClassifierList() {
        return readyClassifierList;
    }

    public void setReadyClassifierList(List<Classifier> readyClassifierList) {
        this.readyClassifierList = readyClassifierList;
    }

    public void addReadyClassifier(Classifier classifier) {
        this.readyClassifierList.add(classifier);
    }

}
