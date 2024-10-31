package org.marcolore.datasetbuilderisw2.utility;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.SelectedTag;

public class ClassifierUtility {

    private ClassifierUtility(){}

    public static RandomForest setRandomForest(){
        return new RandomForest();
    }

    public static NaiveBayes setNaiveBayes(){
        return new NaiveBayes();
    }

    public static IBk setIbk(){
        IBk iBk = new IBk();
        iBk.setDistanceWeighting(new SelectedTag(IBk.WEIGHT_NONE, IBk.TAGS_WEIGHTING));
        return iBk;
    }

    public static MultilayerPerceptron multilayerPerceptron() {
        MultilayerPerceptron multilayerPerceptron = new MultilayerPerceptron();
        multilayerPerceptron.setMomentum(0.2);
        multilayerPerceptron.setLearningRate(0.2);
        multilayerPerceptron.setHiddenLayers("3");
        multilayerPerceptron.setTrainingTime(600);
        return multilayerPerceptron;
    }
}
