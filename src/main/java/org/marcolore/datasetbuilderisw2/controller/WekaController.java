package org.marcolore.datasetbuilderisw2.controller;

import org.marcolore.datasetbuilderisw2.Main;
import org.marcolore.datasetbuilderisw2.model.ConfiguredClassifier;
import org.marcolore.datasetbuilderisw2.model.EvaluationModels;
import org.marcolore.datasetbuilderisw2.utility.WekaUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class WekaController {
    private String project;
    private int iteration;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public WekaController(String project, int iteration) {
        this.project = project;
        this.iteration = iteration;
    }

    public void Classify() throws Exception {
        EvaluationModels evaluationModels = new EvaluationModels();

        for(int i = 1; i < iteration ; i++) {
            Instances trainingSet = WekaUtility.convertData(project, i, "Training");
            Instances testingSet = WekaUtility.convertData(project, i, "Testing");

            if(trainingSet != null) {
                trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
            } else {
                logger.error("Training set null");
            }

            if(testingSet != null){
                testingSet.setClassIndex(testingSet.numAttributes() - 1);
            } else {
                logger.error("Testing set null");
            }


            evaluationModels.setTrainingSet(trainingSet);
            evaluationModels.setTestingSet(testingSet);
            evaluationModels.setConfiguredClassifiers();
            evaluate(evaluationModels);

        }
    }

    public void evaluate(EvaluationModels evaluationModels) throws Exception {
        List<ConfiguredClassifier> configuredClassifiers = evaluationModels.getConfiguredClassifiers();

        for (ConfiguredClassifier classifier : configuredClassifiers){
                Instances trainingSet = evaluationModels.getTrainingSet();
                Instances testingSet = evaluationModels.getTestingSet();

                Evaluation evaluation = new Evaluation(trainingSet);

            }

    }



}
