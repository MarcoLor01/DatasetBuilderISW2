package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

public class ModelEvaluation {

    private String project;
    private int iteration;
    private Classifier classifier;
    private EvaluationMetrics evaluationMetrics;
    private String featureSelection;
    private String balancingMethod;
    private String costSensitive;
    private double trainingPercent;

    public ModelEvaluation(String project, int iteration,
                           Classifier classifier, Evaluation evaluation,
                           String featureSelection, String balancingMethod, String costSensitive) {
        this.project = project;
        this.iteration = iteration;
        this.classifier = classifier;
        this.evaluationMetrics = new EvaluationMetrics(evaluation);
        this.featureSelection = featureSelection;
        this.balancingMethod = balancingMethod;
        this.costSensitive = costSensitive;
    }

    public void setTrainingPercent(double trainingPercent) {
        this.trainingPercent = trainingPercent;
    }

    public String getProject() {
        return project;
    }

    public int getIteration() {
        return iteration;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public EvaluationMetrics getEvaluationMetrics() {
        return evaluationMetrics;
    }

    public String getFeatureSelection() {
        return featureSelection;
    }

    public String getBalancingMethod() {
        return balancingMethod;
    }

    public String getCostSensitive() {
        return costSensitive;
    }

    public double getTrainingPercent() {
        return trainingPercent;
    }
}
