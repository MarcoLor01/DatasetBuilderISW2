package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.util.HashMap;

public class ModelEvaluation {

    private String Project;
    private int iteration;
    private Classifier classifier;
    private EvaluationMetrics evaluationMetrics;
    private String featureSelection;
    private String balancingMethod;
    private String costSensitive;
    private double trainingPercent;

    public ModelEvaluation(String project, int iteration,
                           Classifier classifier, Evaluation evaluation,
                           String featureSelection, String balancingMethod, String costSensitive,
                           double trainingPercent) {
        Project = project;
        this.iteration = iteration;
        this.classifier = classifier;
        this.evaluationMetrics = new EvaluationMetrics(evaluation);
        this.featureSelection = featureSelection;
        this.balancingMethod = balancingMethod;
        this.costSensitive = costSensitive;
        this.trainingPercent = trainingPercent;
    }
}
