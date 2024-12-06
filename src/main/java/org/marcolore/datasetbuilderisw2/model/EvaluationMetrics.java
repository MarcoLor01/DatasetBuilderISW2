package org.marcolore.datasetbuilderisw2.model;

import weka.classifiers.Evaluation;

public class EvaluationMetrics {
    private double precision;
    private double recall;
    private double auc;
    private double kappa;
    private double tp;
    private double fp;
    private double tn;
    private double fn;
    private double cost;
    private double f1;

    public EvaluationMetrics(Evaluation evaluation) {
        this.precision = evaluation.precision(0);
        this.recall = evaluation.recall(0);
        this.auc = evaluation.areaUnderROC(0);
        this.kappa = evaluation.kappa();
        this.tp = evaluation.numTruePositives(0);
        this.fp = evaluation.numFalsePositives(0);
        this.tn = evaluation.numTrueNegatives(0);
        this.fn = evaluation.numFalseNegatives(0);
        this.cost = fp * 1 + fn * 10;
        this.f1 = evaluation.fMeasure(0);
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getAuc() {
        return auc;
    }

    public double getKappa() {
        return kappa;
    }

    public double getTp() {
        return tp;
    }

    public double getFp() {
        return fp;
    }

    public double getTn() {
        return tn;
    }

    public double getFn() {
        return fn;
    }

    public double getCost() {
        return cost;
    }

    public double getF1() {
        return f1;
    }
}
