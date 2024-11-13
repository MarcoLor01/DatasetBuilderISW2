package org.marcolore.datasetbuilderisw2.controller;

import org.marcolore.datasetbuilderisw2.model.AcumeClass;
import org.marcolore.datasetbuilderisw2.model.ConfiguredClassifier;
import org.marcolore.datasetbuilderisw2.model.JavaClass;
import org.marcolore.datasetbuilderisw2.model.ModelEvaluation;
import org.marcolore.datasetbuilderisw2.utility.ClassifierUtility;
import org.marcolore.datasetbuilderisw2.utility.CsvUtility;
import org.marcolore.datasetbuilderisw2.utility.WekaUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.BestFirst;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.supervised.instance.SMOTE;

import java.util.ArrayList;
import java.util.List;

public class WekaController {
    private final String project;
    private final int iteration;
    private static final Logger logger = LoggerFactory.getLogger(WekaController.class);

    private final List<JavaClass> allClasses;
    List<AcumeClass> acumeClasses = new ArrayList<>();


    public WekaController(String project, int iteration, List<JavaClass> allClasses) {
        this.project = project;
        this.iteration = iteration;
        this.allClasses = allClasses;
    }

    public void classify() throws Exception {
        List<ModelEvaluation> modelEvaluationList = new ArrayList<>();

        for(int i = 1; i <= iteration ; i++) {
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

            List<ConfiguredClassifier> configuredClassifiers = setConfiguredClassifiers(trainingSet);
            evaluate(trainingSet, testingSet, configuredClassifiers, modelEvaluationList, i);

        }
    }

    public void evaluate(Instances trainingSet, Instances testingSet, List<ConfiguredClassifier> configuredClassifiers, List<ModelEvaluation> modelEvaluationList, int iteration) throws Exception {

        for (ConfiguredClassifier classifier : configuredClassifiers){

            boolean isFeatureSelection = classifier.isFeatureSelection();
            boolean isBalancingMethod = classifier.isBalancingMethod();
            boolean isCostSensitive = classifier.isCostSensitive();

            for(Classifier readyClassifier : classifier.getReadyClassifierList()){
                readyClassifier.buildClassifier(trainingSet);
                Evaluation evaluation = new Evaluation(trainingSet);
                evaluation.evaluateModel(readyClassifier, testingSet);
                double percentOfTraining = 100.0 * trainingSet.numInstances() / (trainingSet.numInstances() + testingSet.numInstances());

                ModelEvaluation modelEvaluation = new ModelEvaluation(project, iteration, readyClassifier, evaluation,
                        isFeatureSelection ? "Yes" : "No", isBalancingMethod ? "Yes" : "No",
                        isCostSensitive ? "Yes" : "No");
                modelEvaluation.setTrainingPercent(percentOfTraining);

                modelEvaluationList.add(modelEvaluation);
                makePrediction(readyClassifier, testingSet, iteration, isFeatureSelection, isCostSensitive, isBalancingMethod);
            }
            }
    }

    private void makePrediction(Classifier model, Instances testInstances, int numberIteration, boolean isFeatureSelection, boolean isCostSensitive, boolean isBalancingMethod) throws Exception {

        int numTestingInstances = testInstances.numInstances();
        int instanceId = 0;

        acumeClasses.clear();

        List<JavaClass> releaseClasses = allClasses.stream()
                .filter(javaClass -> javaClass.getRelease().getId() == numberIteration + 2)
                .toList();

        if (releaseClasses.size() != numTestingInstances) {
            throw new IllegalStateException("Mismatch between test instances and release classes.");
        }

        for (int i = 0; i < numTestingInstances; i++) {

            JavaClass releaseClass = releaseClasses.get(i);

            var testInstance = testInstances.instance(i);

            String actualClassLabel = testInstance.toString(testInstances.classIndex());
            double[] probabilityDistribution = model.distributionForInstance(testInstance);

            double predictedClassProbability = probabilityDistribution[0];


            AcumeClass acumeEntry = new AcumeClass(
                    instanceId,
                    releaseClass.getLoc(),
                    predictedClassProbability,
                    actualClassLabel
            );

            acumeClasses.add(acumeEntry);

            instanceId++;
        }
        String classifierName = WekaUtility.getClassifierName(model);
        CsvUtility.createAcumeFiles(project, acumeClasses, isBalancingMethod, isFeatureSelection, isCostSensitive, numberIteration, classifierName);
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

    public List<ConfiguredClassifier> setConfiguredClassifiers(Instances trainingSet) {

        boolean[] costSensitiveOptions = {false, true};
        boolean[] featureSelectionOptions = {false, true};
        boolean[] balanceMethodOptions = {false, true};
        List<ConfiguredClassifier> configuredClassifierList = new ArrayList<>();
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

                    prepareClassifier(configuredClassifier, trainingSet, configuredClassifierList);
                }
            }
        }

        return configuredClassifierList;
    }

    void prepareClassifier(ConfiguredClassifier configuredClassifier, Instances trainingSet, List<ConfiguredClassifier> configuredClassifierList) {

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
            }

            if(configuredClassifier.isFeatureSelection()) {
                currentClassifier = createModelFeatureSelection(currentClassifier);
            }

            if(configuredClassifier.isBalancingMethod()) {
                currentClassifier = createModelSMOTE(currentClassifier, sizeMajorClass, sizeMinorClass);
            }

            configuredClassifier.addReadyClassifier(currentClassifier);
        }

        configuredClassifierList.add(configuredClassifier);

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
        costMatrix.setCell(1,0,1.0);  //Last value -> weight of false positive
        costMatrix.setCell(0,1,10.0); //Last value -> weight of false negative
        costMatrix.setCell(1,1,0.0);
        costSensitiveClassifier.setMinimizeExpectedCost(false);

        costSensitiveClassifier.setCostMatrix(costMatrix);
        return costSensitiveClassifier;
    }



}
