package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.util.ArrayList;
import java.util.List;

public class ConfiguredClassifier {

    private List<Classifier> baseClassifierList = new ArrayList<>();
    private List<Classifier> readyClassifierList = new ArrayList<>();
    private List<Evaluation> classifierEvaluationList = new ArrayList<>();
    private boolean balancingMethod;
    private boolean featureSelection;
    private boolean costSensitive;
    private Evaluation randomForestEvaluation;
    private Evaluation naiveBayesEvaluation;
    private Evaluation ibkEvaluation;

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

    public Evaluation getRandomForestEvaluation() {
        return randomForestEvaluation;
    }

    public void setRandomForestEvaluation(Evaluation randomForestEvaluation) {
        this.randomForestEvaluation = randomForestEvaluation;
    }

    public Evaluation getNaiveBayesEvaluation() {
        return naiveBayesEvaluation;
    }

    public void setNaiveBayesEvaluation(Evaluation naiveBayesEvaluation) {
        this.naiveBayesEvaluation = naiveBayesEvaluation;
    }

    public Evaluation getIbkEvaluation() {
        return ibkEvaluation;
    }

    public void setIbkEvaluation(Evaluation ibkEvaluation) {
        this.ibkEvaluation = ibkEvaluation;
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

    public List<Evaluation> getClassifierEvaluationList() {
        return classifierEvaluationList;
    }

    public void setClassifierEvaluationList(List<Evaluation> classifierEvaluationList) {
        this.classifierEvaluationList = classifierEvaluationList;
    }

    public void addClassifierEvaluation(Evaluation evaluation) {
        this.classifierEvaluationList.add(evaluation);
    }
}
